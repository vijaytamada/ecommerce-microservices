package com.company.shipping_service.service.impl;

import com.company.shipping_service.config.RabbitMQConfig;
import com.company.shipping_service.dto.response.ShipmentResponse;
import com.company.shipping_service.entity.Shipment;
import com.company.shipping_service.entity.Shipment.ShipmentStatus;
import com.company.shipping_service.entity.TrackingEvent;
import com.company.shipping_service.exception.ResourceNotFoundException;
import com.company.shipping_service.messaging.event.ShipmentEvent;
import com.company.shipping_service.repository.ShipmentRepository;
import com.company.shipping_service.service.ShippingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShippingServiceImpl implements ShippingService {

    private final ShipmentRepository shipmentRepository;
    private final RabbitTemplate rabbitTemplate;

    @Value("${shiprocket.simulate:true}")
    private boolean simulate;

    @Value("${shiprocket.base-url:https://apiv2.shiprocket.in/v1/external}")
    private String shiprocketBaseUrl;

    @Override
    @Transactional
    public ShipmentResponse createShipment(Map<String, Object> orderEvent) {
        UUID orderId = UUID.fromString((String) orderEvent.get("orderId"));
        UUID userId  = UUID.fromString((String) orderEvent.get("userId"));
        String address = (String) orderEvent.getOrDefault("shippingAddress", "{}");

        // When simulate=true: auto-generate tracking number (no real ShipRocket calls)
        // When simulate=false: call ShipRocket API at shiprocketBaseUrl to create a real shipment order
        String trackingNumber = simulate
                ? "TRK-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase()
                : callShipRocket(orderId, address);
        log.info("Shipment mode: {}", simulate ? "SIMULATED" : "SHIPROCKET (" + shiprocketBaseUrl + ")");

        TrackingEvent initial = TrackingEvent.builder()
                .status("PROCESSING")
                .location("Warehouse")
                .description("Order received at warehouse. Preparing for dispatch.")
                .build();

        Shipment shipment = Shipment.builder()
                .orderId(orderId)
                .userId(userId)
                .trackingNumber(trackingNumber)
                .carrier("LOCAL_COURIER")
                .status(ShipmentStatus.PROCESSING)
                .estimatedDelivery(LocalDate.now().plusDays(5))
                .shippingAddress(address)
                .trackingEvents(new ArrayList<>(List.of(initial)))
                .build();

        initial.setShipment(shipment);
        shipment = shipmentRepository.save(shipment);
        log.info("Shipment created: trackingNumber={} for orderId={}", trackingNumber, orderId);
        return toResponse(shipment);
    }

    @Override
    public ShipmentResponse getShipmentByOrder(UUID orderId) {
        return toResponse(shipmentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found for order: " + orderId)));
    }

    @Override
    public ShipmentResponse getShipment(UUID shipmentId) {
        return toResponse(findOrThrow(shipmentId));
    }

    @Override
    public ShipmentResponse trackByNumber(String trackingNumber) {
        return toResponse(shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found: " + trackingNumber)));
    }

    @Override
    @Transactional
    public ShipmentResponse updateStatus(UUID shipmentId, ShipmentStatus status) {
        Shipment shipment = findOrThrow(shipmentId);
        shipment.setStatus(status);

        if (status == ShipmentStatus.DELIVERED) {
            shipment.setActualDelivery(LocalDate.now());
        }

        // Add a tracking event
        TrackingEvent event = TrackingEvent.builder()
                .shipment(shipment)
                .status(status.name())
                .location("In Transit")
                .description("Status updated to " + status.name())
                .build();

        if (shipment.getTrackingEvents() == null) shipment.setTrackingEvents(new ArrayList<>());
        shipment.getTrackingEvents().add(event);

        shipment = shipmentRepository.save(shipment);

        // Publish events for key status changes
        if (status == ShipmentStatus.DISPATCHED || status == ShipmentStatus.DELIVERED) {
            String routingKey = status == ShipmentStatus.DISPATCHED
                    ? RabbitMQConfig.SHIPMENT_DISPATCHED_KEY
                    : RabbitMQConfig.SHIPMENT_DELIVERED_KEY;

            ShipmentEvent shipmentEvent = ShipmentEvent.builder()
                    .eventType(status == ShipmentStatus.DISPATCHED ? "SHIPMENT_DISPATCHED" : "SHIPMENT_DELIVERED")
                    .shipmentId(shipment.getId())
                    .orderId(shipment.getOrderId())
                    .userId(shipment.getUserId())
                    .trackingNumber(shipment.getTrackingNumber())
                    .carrier(shipment.getCarrier())
                    .timestamp(Instant.now())
                    .build();

            rabbitTemplate.convertAndSend(RabbitMQConfig.SHIPPING_EXCHANGE, routingKey, shipmentEvent);
            log.info("Published {} for shipmentId={}", shipmentEvent.getEventType(), shipmentId);
        }

        return toResponse(shipment);
    }

    /**
     * Stub for real ShipRocket integration.
     * To implement: POST to shiprocketBaseUrl/orders/create/adhoc with JWT auth token.
     * Login: POST /auth/login with email+password → get token → use as Bearer.
     * Set shiprocket.simulate=false in application.yml to activate.
     */
    private String callShipRocket(UUID orderId, String address) {
        // TODO: Use RestTemplate/WebClient to:
        // 1. POST /auth/login → get token
        // 2. POST /orders/create/adhoc → get shipment_id + awb_code (tracking number)
        throw new UnsupportedOperationException("Real ShipRocket integration not yet implemented. Set shiprocket.simulate=true");
    }

    /* ---- Helpers ---- */

    private Shipment findOrThrow(UUID id) {
        return shipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found: " + id));
    }

    private ShipmentResponse toResponse(Shipment s) {
        List<ShipmentResponse.TrackingEventResponse> events = s.getTrackingEvents() == null ? List.of() :
                s.getTrackingEvents().stream().map(e -> ShipmentResponse.TrackingEventResponse.builder()
                        .status(e.getStatus()).location(e.getLocation())
                        .description(e.getDescription()).eventTime(e.getEventTime())
                        .build()).toList();

        return ShipmentResponse.builder()
                .id(s.getId()).orderId(s.getOrderId()).userId(s.getUserId())
                .carrier(s.getCarrier()).trackingNumber(s.getTrackingNumber())
                .status(s.getStatus()).estimatedDelivery(s.getEstimatedDelivery())
                .actualDelivery(s.getActualDelivery()).shippingAddress(s.getShippingAddress())
                .trackingEvents(events).createdAt(s.getCreatedAt())
                .build();
    }
}
