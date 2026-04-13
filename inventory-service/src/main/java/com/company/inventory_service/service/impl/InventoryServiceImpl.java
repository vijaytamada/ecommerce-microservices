package com.company.inventory_service.service.impl;

import com.company.inventory_service.config.RabbitMQConfig;
import com.company.inventory_service.dto.request.BulkCheckRequest;
import com.company.inventory_service.dto.request.ReserveRequest;
import com.company.inventory_service.dto.request.StockRequest;
import com.company.inventory_service.dto.response.BulkAvailabilityResponse;
import com.company.inventory_service.dto.response.InventoryResponse;
import com.company.inventory_service.entity.InventoryItem;
import com.company.inventory_service.entity.StockReservation;
import com.company.inventory_service.entity.StockReservation.ReservationStatus;
import com.company.inventory_service.exception.InsufficientStockException;
import com.company.inventory_service.exception.ResourceNotFoundException;
import com.company.inventory_service.messaging.event.InventoryEvent;
import com.company.inventory_service.repository.InventoryItemRepository;
import com.company.inventory_service.repository.StockReservationRepository;
import com.company.inventory_service.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryItemRepository inventoryRepo;
    private final StockReservationRepository reservationRepo;
    private final RabbitTemplate rabbitTemplate;

    @Override
    public InventoryResponse createStock(StockRequest request) {
        InventoryItem item = InventoryItem.builder()
                .productId(request.getProductId())
                .quantityAvailable(request.getQuantity())
                .lowStockThreshold(request.getLowStockThreshold() != null ? request.getLowStockThreshold() : 5)
                .build();
        return toResponse(inventoryRepo.save(item));
    }

    @Override
    public InventoryResponse getStock(String productId) {
        return toResponse(findOrThrow(productId));
    }

    @Override
    @Transactional
    public InventoryResponse updateStock(String productId, int quantity) {
        InventoryItem item = findOrThrow(productId);
        item.setQuantityAvailable(item.getQuantityAvailable() + quantity);
        item = inventoryRepo.save(item);
        checkAndPublishLowStock(item);
        return toResponse(item);
    }

    @Override
    public List<InventoryResponse> getLowStockItems() {
        return inventoryRepo.findAll().stream()
                .filter(i -> i.getQuantityAvailable() <= i.getLowStockThreshold())
                .map(this::toResponse)
                .toList();
    }

    @Override
    public BulkAvailabilityResponse bulkCheck(BulkCheckRequest request) {
        Map<String, Boolean> availability = new HashMap<>();
        for (ReserveRequest item : request.getItems()) {
            inventoryRepo.findById(item.getProductId()).ifPresentOrElse(
                inv -> availability.put(item.getProductId(), inv.getQuantityAvailable() >= item.getQuantity()),
                () -> availability.put(item.getProductId(), false)
            );
        }
        boolean allAvailable = availability.values().stream().allMatch(Boolean::booleanValue);
        return BulkAvailabilityResponse.builder().allAvailable(allAvailable).availability(availability).build();
    }

    @Override
    @Transactional
    public void reserve(ReserveRequest request) {
        InventoryItem item = findOrThrow(request.getProductId());
        if (item.getQuantityAvailable() < request.getQuantity()) {
            throw new InsufficientStockException(
                    "Insufficient stock for product " + request.getProductId() +
                    ". Available: " + item.getQuantityAvailable() + ", Requested: " + request.getQuantity());
        }
        item.setQuantityAvailable(item.getQuantityAvailable() - request.getQuantity());
        item.setQuantityReserved(item.getQuantityReserved() + request.getQuantity());
        inventoryRepo.save(item);

        StockReservation reservation = StockReservation.builder()
                .orderId(request.getOrderId())
                .productId(request.getProductId())
                .quantity(request.getQuantity())
                .status(ReservationStatus.PENDING)
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .build();
        reservationRepo.save(reservation);
        log.info("Reserved {} units of {} for orderId={}", request.getQuantity(), request.getProductId(), request.getOrderId());
    }

    @Override
    @Transactional
    public void confirmReservation(UUID orderId) {
        List<StockReservation> reservations = reservationRepo.findByOrderIdAndStatus(orderId, ReservationStatus.PENDING);
        for (StockReservation r : reservations) {
            InventoryItem item = findOrThrow(r.getProductId());
            item.setQuantityReserved(item.getQuantityReserved() - r.getQuantity());
            inventoryRepo.save(item);
            r.setStatus(ReservationStatus.CONFIRMED);
            reservationRepo.save(r);
            checkAndPublishLowStock(item);
        }
        log.info("Confirmed reservations for orderId={}", orderId);
    }

    @Override
    @Transactional
    public void releaseReservation(UUID orderId) {
        List<StockReservation> reservations = reservationRepo.findByOrderIdAndStatus(orderId, ReservationStatus.PENDING);
        for (StockReservation r : reservations) {
            InventoryItem item = findOrThrow(r.getProductId());
            item.setQuantityAvailable(item.getQuantityAvailable() + r.getQuantity());
            item.setQuantityReserved(item.getQuantityReserved() - r.getQuantity());
            inventoryRepo.save(item);
            r.setStatus(ReservationStatus.RELEASED);
            reservationRepo.save(r);
        }
        log.info("Released reservations for orderId={}", orderId);
    }

    /* ---- Helpers ---- */

    private InventoryItem findOrThrow(String productId) {
        return inventoryRepo.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product: " + productId));
    }

    private void checkAndPublishLowStock(InventoryItem item) {
        if (item.getQuantityAvailable() <= item.getLowStockThreshold()) {
            InventoryEvent event = InventoryEvent.builder()
                    .eventType("INVENTORY_LOW_STOCK")
                    .productId(item.getProductId())
                    .quantityAvailable(item.getQuantityAvailable())
                    .threshold(item.getLowStockThreshold())
                    .timestamp(Instant.now())
                    .build();
            rabbitTemplate.convertAndSend(RabbitMQConfig.INVENTORY_EXCHANGE, RabbitMQConfig.INVENTORY_LOW_STOCK_KEY, event);
            log.warn("Low stock alert published for productId={}", item.getProductId());
        }
    }

    private InventoryResponse toResponse(InventoryItem i) {
        return InventoryResponse.builder()
                .productId(i.getProductId())
                .quantityAvailable(i.getQuantityAvailable())
                .quantityReserved(i.getQuantityReserved())
                .lowStockThreshold(i.getLowStockThreshold())
                .inStock(i.getQuantityAvailable() > 0)
                .updatedAt(i.getUpdatedAt())
                .build();
    }
}
