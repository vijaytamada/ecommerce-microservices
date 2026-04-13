package com.company.order_service.service.impl;

import com.company.order_service.client.InventoryClient;
import com.company.order_service.client.ProductClient;
import com.company.order_service.dto.request.PlaceOrderRequest;
import com.company.order_service.dto.response.OrderResponse;
import com.company.order_service.entity.Order;
import com.company.order_service.entity.Order.OrderStatus;
import com.company.order_service.entity.OrderItem;
import com.company.order_service.exception.ResourceNotFoundException;
import com.company.order_service.messaging.event.OrderEvent;
import com.company.order_service.messaging.publisher.OrderEventPublisher;
import com.company.order_service.repository.OrderRepository;
import com.company.order_service.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final InventoryClient inventoryClient;
    private final OrderEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public OrderResponse placeOrder(UUID userId, String userEmail, PlaceOrderRequest request) {
        List<OrderItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (PlaceOrderRequest.OrderItemRequest itemReq : request.getItems()) {
            // Fetch product snapshot
            Map<String, Object> productData = fetchProduct(itemReq.getProductId());
            String productName = (String) productData.get("name");
            BigDecimal price = new BigDecimal(productData.get("price").toString());

            // Reserve stock
            Map<String, Object> reserveBody = new HashMap<>();
            reserveBody.put("orderId", UUID.randomUUID()); // temp; replaced after order saved
            reserveBody.put("productId", itemReq.getProductId());
            reserveBody.put("quantity", itemReq.getQuantity());
            inventoryClient.reserve(reserveBody);

            BigDecimal subtotal = price.multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            total = total.add(subtotal);

            items.add(OrderItem.builder()
                    .productId(itemReq.getProductId())
                    .productName(productName)
                    .quantity(itemReq.getQuantity())
                    .unitPrice(price)
                    .subtotal(subtotal)
                    .build());
        }

        String addressJson = toJson(request.getShippingAddress());

        Order order = Order.builder()
                .userId(userId)
                .totalAmount(total)
                .shippingAddress(addressJson)
                .status(OrderStatus.PENDING)
                .build();

        order = orderRepository.save(order);
        Order savedOrder = order;
        items.forEach(i -> i.setOrder(savedOrder));
        savedOrder.setItems(items);
        orderRepository.save(savedOrder);

        // Publish ORDER_CREATED → Payment Service picks it up
        eventPublisher.publishOrderCreated(buildEvent("ORDER_CREATED", savedOrder, userEmail));
        log.info("Order placed: id={}", savedOrder.getId());
        return toResponse(savedOrder);
    }

    @Override
    public OrderResponse getOrder(UUID orderId) {
        return toResponse(findOrThrow(orderId));
    }

    @Override
    public Page<OrderResponse> getMyOrders(UUID userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable).map(this::toResponse);
    }

    @Override
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    @Transactional
    public OrderResponse updateStatus(UUID orderId, OrderStatus status) {
        Order order = findOrThrow(orderId);
        order.setStatus(status);
        order = orderRepository.save(order);

        OrderEvent event = buildEvent("ORDER_" + status.name(), order, null);
        switch (status) {
            case CONFIRMED  -> eventPublisher.publishOrderConfirmed(event);
            case CANCELLED  -> eventPublisher.publishOrderCancelled(event);
            case SHIPPED    -> eventPublisher.publishOrderShipped(event);
            case DELIVERED  -> eventPublisher.publishOrderDelivered(event);
            default -> {}
        }
        return toResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(UUID orderId, UUID userId) {
        Order order = findOrThrow(orderId);
        if (!order.getUserId().equals(userId)) {
            throw new IllegalStateException("You cannot cancel this order");
        }
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Order cannot be cancelled in status: " + order.getStatus());
        }
        order.setStatus(OrderStatus.CANCELLED);
        order = orderRepository.save(order);
        eventPublisher.publishOrderCancelled(buildEvent("ORDER_CANCELLED", order, null));
        return toResponse(order);
    }

    /* ---- Helpers ---- */

    private Order findOrThrow(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> fetchProduct(String productId) {
        try {
            var response = productClient.getProduct(productId);
            return (Map<String, Object>) response.getData();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to fetch product " + productId + ": " + e.getMessage());
        }
    }

    private String toJson(Object obj) {
        try { return objectMapper.writeValueAsString(obj); }
        catch (Exception e) { return "{}"; }
    }

    private OrderEvent buildEvent(String type, Order order, String userEmail) {
        List<OrderEvent.OrderItemInfo> itemInfos = order.getItems() == null ? List.of() :
                order.getItems().stream().map(i -> OrderEvent.OrderItemInfo.builder()
                        .productId(i.getProductId())
                        .productName(i.getProductName())
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .build()).toList();

        return OrderEvent.builder()
                .eventType(type)
                .orderId(order.getId())
                .userId(order.getUserId())
                .userEmail(userEmail)
                .totalAmount(order.getTotalAmount())
                .currency(order.getCurrency())
                .items(itemInfos)
                .shippingAddress(order.getShippingAddress())
                .timestamp(Instant.now())
                .build();
    }

    private OrderResponse toResponse(Order o) {
        List<OrderResponse.OrderItemResponse> itemResponses = o.getItems() == null ? List.of() :
                o.getItems().stream().map(i -> OrderResponse.OrderItemResponse.builder()
                        .id(i.getId()).productId(i.getProductId()).productName(i.getProductName())
                        .quantity(i.getQuantity()).unitPrice(i.getUnitPrice()).subtotal(i.getSubtotal())
                        .build()).toList();

        return OrderResponse.builder()
                .id(o.getId()).userId(o.getUserId()).status(o.getStatus())
                .totalAmount(o.getTotalAmount()).currency(o.getCurrency())
                .shippingAddress(o.getShippingAddress()).items(itemResponses)
                .createdAt(o.getCreatedAt()).updatedAt(o.getUpdatedAt())
                .build();
    }
}
