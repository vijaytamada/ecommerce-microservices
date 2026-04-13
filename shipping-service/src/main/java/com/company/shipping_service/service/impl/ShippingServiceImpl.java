package com.company.shipping_service.service.impl;

import com.company.shipping_service.client.IThinkClient;
import com.company.shipping_service.config.IThinkProperties;
import com.company.shipping_service.config.RabbitMQConfig;
import com.company.shipping_service.dto.response.ShipmentResponse;
import com.company.shipping_service.entity.Shipment;
import com.company.shipping_service.entity.Shipment.ShipmentStatus;
import com.company.shipping_service.entity.TrackingEvent;
import com.company.shipping_service.exception.ResourceNotFoundException;
import com.company.shipping_service.messaging.event.ShipmentEvent;
import com.company.shipping_service.repository.ShipmentRepository;
import com.company.shipping_service.repository.TrackingEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShippingServiceImpl implements com.company.shipping_service.service.ShippingService {

    private final ShipmentRepository       shipmentRepository;
    private final TrackingEventRepository  trackingEventRepository;
    private final IThinkClient             iThinkClient;
    private final IThinkProperties         props;
    private final RabbitTemplate           rabbitTemplate;

    /** Poll interval: 2 hours */
    private static final long POLL_INTERVAL_MINUTES = 120;

    // ── iThink forward-shipment status → internal status ────────────────────
    private static final Map<String, ShipmentStatus> FORWARD_STATUS_MAP = new LinkedHashMap<>();
    static {
        FORWARD_STATUS_MAP.put("Manifested",                ShipmentStatus.MANIFESTED);
        FORWARD_STATUS_MAP.put("Picked Up",                 ShipmentStatus.DISPATCHED);
        FORWARD_STATUS_MAP.put("In Transit",                ShipmentStatus.IN_TRANSIT);
        FORWARD_STATUS_MAP.put("Reached At Destination",    ShipmentStatus.IN_TRANSIT);
        FORWARD_STATUS_MAP.put("Out For Delivery",          ShipmentStatus.OUT_FOR_DELIVERY);
        FORWARD_STATUS_MAP.put("Undelivered",               ShipmentStatus.OUT_FOR_DELIVERY);
        FORWARD_STATUS_MAP.put("Delivered",                 ShipmentStatus.DELIVERED);
        FORWARD_STATUS_MAP.put("RTO Pending",               ShipmentStatus.RTO);
        FORWARD_STATUS_MAP.put("RTO Processing",            ShipmentStatus.RTO);
        FORWARD_STATUS_MAP.put("RTO In Transit",            ShipmentStatus.RTO);
        FORWARD_STATUS_MAP.put("Reached At Origin",         ShipmentStatus.RTO);
        FORWARD_STATUS_MAP.put("RTO Out For Delivery",      ShipmentStatus.RTO);
        FORWARD_STATUS_MAP.put("RTO Undelivered",           ShipmentStatus.RTO);
        FORWARD_STATUS_MAP.put("RTO Delivered",             ShipmentStatus.RTO);
        FORWARD_STATUS_MAP.put("Cancelled",                 ShipmentStatus.CANCELLED);
    }

    // ────────────────────────────────────────────────────────────────────────
    // Called from RabbitMQ consumer when order.confirmed event arrives.
    // Just creates the DB record; admin must then call createShipmentOnIthink()
    // to actually push to iThink and get an AWB.
    // ────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public ShipmentResponse createShipment(Map<String, Object> orderEvent) {
        UUID orderId = UUID.fromString((String) orderEvent.get("orderId"));
        UUID userId  = UUID.fromString((String) orderEvent.get("userId"));
        String shippingAddress = (String) orderEvent.getOrDefault("shippingAddress", "{}");

        // Idempotency: don't create a second record for the same order
        Optional<Shipment> existing = shipmentRepository.findByOrderId(orderId);
        if (existing.isPresent()) {
            log.warn("Shipment already exists for orderId={}, skipping creation", orderId);
            return toResponse(existing.get());
        }

        Shipment shipment = Shipment.builder()
                .orderId(orderId)
                .userId(userId)
                .status(ShipmentStatus.PROCESSING)
                .ithinkStatus("Pending AWB")
                .estimatedDelivery(LocalDate.now().plusDays(5))
                .shippingAddress(shippingAddress)
                .trackingEvents(new ArrayList<>())
                .build();

        shipment = shipmentRepository.save(shipment);
        log.info("Shipment DB record created for orderId={} — awaiting iThink AWB assignment", orderId);
        return toResponse(shipment);
    }

    // ────────────────────────────────────────────────────────────────────────
    // Admin action: push order to iThink and assign AWB.
    // simulate=true  → generates a fake TRK-XXXXXX AWB, no real API call.
    // simulate=false → calls iThink API v3.
    // ────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public ShipmentResponse createShipmentOnIthink(UUID orderId) {
        Shipment shipment = shipmentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found for order: " + orderId));

        if (shipment.getAwb() != null) {
            log.warn("AWB already assigned for orderId={} (awb={})", orderId, shipment.getAwb());
            return toResponse(shipment);
        }

        if (props.isSimulate()) {
            // Simulation mode — no real iThink call
            String fakeAwb = "TRK-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();
            shipment.setAwb(fakeAwb);
            shipment.setCourierName("SIMULATED_COURIER");
            shipment.setIthinkStatus("Manifested");
            shipment.setStatus(ShipmentStatus.MANIFESTED);
            shipment.setNextSyncAt(LocalDateTime.now().plusMinutes(POLL_INTERVAL_MINUTES));
            shipment = shipmentRepository.save(shipment);
            log.info("[SIMULATE] AWB assigned: {} for orderId={}", fakeAwb, orderId);
            return toResponse(shipment);
        }

        // Build iThink shipment payload from the stored shippingAddress JSON
        Map<String, Object> addressMap = parseAddressJson(shipment.getShippingAddress());
        String orderDate = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("waybill",      "");
        payload.put("order",        orderId.toString());
        payload.put("sub_order",    "");
        payload.put("order_date",   orderDate);
        payload.put("total_amount", addressMap.getOrDefault("totalAmount", "0").toString());

        String fullName = addressMap.getOrDefault("fullName",
                addressMap.getOrDefault("street", "Customer")).toString();

        payload.put("name",         fullName);
        payload.put("company_name", "");
        payload.put("add",          addressMap.getOrDefault("street", "").toString());
        payload.put("add2",         "");
        payload.put("add3",         "");
        payload.put("pin",          addressMap.getOrDefault("zipCode", "").toString());
        payload.put("city",         addressMap.getOrDefault("city", "").toString());
        payload.put("state",        addressMap.getOrDefault("state", "").toString());
        payload.put("country",      addressMap.getOrDefault("country", "India").toString());
        payload.put("phone",        addressMap.getOrDefault("phone", "").toString());
        payload.put("alt_phone",    "");
        payload.put("email",        "");

        payload.put("is_billing_same_as_shipping", "yes");
        payload.put("billing_name",         fullName);
        payload.put("billing_company_name", "");
        payload.put("billing_add",          addressMap.getOrDefault("street", "").toString());
        payload.put("billing_add2",         "");
        payload.put("billing_add3",         "");
        payload.put("billing_pin",          addressMap.getOrDefault("zipCode", "").toString());
        payload.put("billing_city",         addressMap.getOrDefault("city", "").toString());
        payload.put("billing_state",        addressMap.getOrDefault("state", "").toString());
        payload.put("billing_country",      addressMap.getOrDefault("country", "India").toString());
        payload.put("billing_phone",        addressMap.getOrDefault("phone", "").toString());
        payload.put("billing_alt_phone",    "");
        payload.put("billing_email",        "");

        payload.put("products", List.of(Map.of(
                "product_name",     "Order Items",
                "product_sku",      orderId.toString().substring(0, 8),
                "product_quantity", "1",
                "product_price",    addressMap.getOrDefault("totalAmount", "0").toString()
        )));

        payload.put("shipment_length",       "10");
        payload.put("shipment_width",        "10");
        payload.put("shipment_height",       "10");
        payload.put("weight",                "0.5");
        payload.put("shipping_charges",      "0");
        payload.put("giftwrap_charges",      "0");
        payload.put("transaction_charges",   "0");
        payload.put("total_discount",        "0");
        payload.put("first_attemp_discount", "0");
        payload.put("cod_charges",           "0");
        payload.put("advance_amount",        "0");
        payload.put("cod_amount",            "0");
        payload.put("payment_mode",          "PREPAID");
        payload.put("reseller_name",         "");
        payload.put("eway_bill_number",      "");
        payload.put("gst_number",            "");
        payload.put("return_address_id",     props.getReturnAddressId());

        Map<String, Object> itlRes = iThinkClient.createOrder(payload, "forward");

        Integer statusCode = (Integer) itlRes.get("status_code");
        if (statusCode == null || statusCode != 200) {
            throw new RuntimeException("iThink order creation failed: " + itlRes.get("html_message"));
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> dataMap = (Map<String, Object>) itlRes.get("data");
        if (dataMap == null || dataMap.isEmpty()) {
            throw new RuntimeException("iThink returned empty data map");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) dataMap.values().iterator().next();
        if (!"success".equalsIgnoreCase((String) result.get("status"))) {
            throw new RuntimeException("iThink rejected shipment: " + result.get("remark"));
        }

        String awb = result.get("waybill").toString();

        // Fetch label & manifest (non-fatal if unavailable immediately)
        String labelUrl    = null;
        String manifestUrl = null;
        try {
            Map<String, Object> labelRes = iThinkClient.getLabel(awb);
            labelUrl = (String) labelRes.get("file_name");
        } catch (Exception e) {
            log.warn("[iThink] Label fetch failed for awb={}: {}", awb, e.getMessage());
        }
        try {
            Map<String, Object> manifestRes = iThinkClient.getManifest(awb);
            manifestUrl = (String) manifestRes.get("file_name");
        } catch (Exception e) {
            log.warn("[iThink] Manifest fetch failed for awb={}: {}", awb, e.getMessage());
        }

        shipment.setAwb(awb);
        shipment.setProviderRef((String) result.get("refnum"));
        shipment.setCourierName((String) result.get("logistic_name"));
        shipment.setIthinkStatus("Manifested");
        shipment.setStatus(ShipmentStatus.MANIFESTED);
        shipment.setLabelUrl(labelUrl);
        shipment.setManifestUrl(manifestUrl);
        shipment.setNextSyncAt(LocalDateTime.now().plusMinutes(POLL_INTERVAL_MINUTES));

        shipment = shipmentRepository.save(shipment);
        log.info("[iThink] AWB={} assigned for orderId={}", awb, orderId);
        return toResponse(shipment);
    }

    // ────────────────────────────────────────────────────────────────────────
    // Pincode serviceability check (public)
    // ────────────────────────────────────────────────────────────────────────
    @Override
    public Map<String, Object> checkPincode(String pincode) {
        if (props.isSimulate()) {
            return Map.of("pincode", pincode, "serviceable", true, "note", "Simulation mode — all pincodes serviceable");
        }
        Map<String, Object> res = iThinkClient.checkPincode(pincode);
        @SuppressWarnings("unchecked")
        Map<String, Object> pincodeData = (Map<String, Object>) ((Map<String, Object>) res.getOrDefault("data", Map.of())).get(pincode);
        return Map.of("pincode", pincode, "data", pincodeData != null ? pincodeData : Map.of(),
                "serviceable", pincodeData != null);
    }

    // ────────────────────────────────────────────────────────────────────────
    // Queries
    // ────────────────────────────────────────────────────────────────────────
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
    public ShipmentResponse trackByNumber(String awb) {
        return toResponse(shipmentRepository.findByAwb(awb)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found for AWB: " + awb)));
    }

    // ────────────────────────────────────────────────────────────────────────
    // Force-sync tracking from iThink
    // ────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public ShipmentResponse syncTracking(UUID shipmentId) {
        Shipment shipment = findOrThrow(shipmentId);
        if (shipment.getAwb() == null) {
            throw new IllegalStateException("No AWB assigned yet for shipment: " + shipmentId);
        }
        return toResponse(syncFromIthink(shipment));
    }

    // ────────────────────────────────────────────────────────────────────────
    // Cancel on iThink
    // ────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public ShipmentResponse cancelShipmentOnIthink(UUID shipmentId) {
        Shipment shipment = findOrThrow(shipmentId);

        if (!props.isSimulate() && shipment.getAwb() != null) {
            try {
                Map<String, Object> res = iThinkClient.cancelOrder(shipment.getAwb());
                log.info("[iThink] Cancel response for awb={}: {}", shipment.getAwb(), res);
            } catch (Exception e) {
                log.warn("[iThink] Cancel call failed for awb={}: {} — marking cancelled locally", shipment.getAwb(), e.getMessage());
            }
        }

        shipment.setStatus(ShipmentStatus.CANCELLED);
        shipment.setIthinkStatus("Cancelled");
        return toResponse(shipmentRepository.save(shipment));
    }

    // ────────────────────────────────────────────────────────────────────────
    // Re-fetch label & manifest
    // ────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public ShipmentResponse generateDocuments(UUID shipmentId) {
        Shipment shipment = findOrThrow(shipmentId);
        if (shipment.getAwb() == null) {
            throw new IllegalStateException("No AWB assigned yet — cannot fetch documents");
        }
        if (props.isSimulate()) {
            shipment.setLabelUrl("https://simulate.example.com/label/" + shipment.getAwb() + ".pdf");
            shipment.setManifestUrl("https://simulate.example.com/manifest/" + shipment.getAwb() + ".pdf");
            return toResponse(shipmentRepository.save(shipment));
        }

        String labelUrl = null, manifestUrl = null;
        try { labelUrl    = (String) iThinkClient.getLabel(shipment.getAwb()).get("file_name"); }
        catch (Exception e) { log.warn("[iThink] Label fetch failed: {}", e.getMessage()); }
        try { manifestUrl = (String) iThinkClient.getManifest(shipment.getAwb()).get("file_name"); }
        catch (Exception e) { log.warn("[iThink] Manifest fetch failed: {}", e.getMessage()); }

        if (labelUrl    != null) shipment.setLabelUrl(labelUrl);
        if (manifestUrl != null) shipment.setManifestUrl(manifestUrl);
        return toResponse(shipmentRepository.save(shipment));
    }

    // ────────────────────────────────────────────────────────────────────────
    // Manual status update (admin) — also publishes events for key transitions
    // ────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public ShipmentResponse updateStatus(UUID shipmentId, ShipmentStatus status) {
        Shipment shipment = findOrThrow(shipmentId);
        shipment.setStatus(status);

        if (status == ShipmentStatus.DELIVERED) {
            shipment.setActualDelivery(LocalDate.now());
        }

        TrackingEvent event = TrackingEvent.builder()
                .shipment(shipment)
                .status(status.name())
                .location("In Transit")
                .description("Status manually updated to " + status.name())
                .eventTime(LocalDateTime.now())
                .uniqueKey(shipment.getAwb() + "::" + LocalDateTime.now() + "::" + status.name())
                .build();

        if (shipment.getTrackingEvents() == null) shipment.setTrackingEvents(new ArrayList<>());
        shipment.getTrackingEvents().add(event);
        shipment = shipmentRepository.save(shipment);

        // Publish events for key transitions
        if (status == ShipmentStatus.DISPATCHED || status == ShipmentStatus.DELIVERED) {
            String eventType   = status == ShipmentStatus.DISPATCHED ? "SHIPMENT_DISPATCHED" : "SHIPMENT_DELIVERED";
            String routingKey  = status == ShipmentStatus.DISPATCHED
                    ? RabbitMQConfig.SHIPMENT_DISPATCHED_KEY
                    : RabbitMQConfig.SHIPMENT_DELIVERED_KEY;

            ShipmentEvent shipmentEvent = ShipmentEvent.builder()
                    .eventType(eventType)
                    .shipmentId(shipment.getId())
                    .orderId(shipment.getOrderId())
                    .userId(shipment.getUserId())
                    .awb(shipment.getAwb())
                    .courierName(shipment.getCourierName())
                    .ithinkStatus(shipment.getIthinkStatus())
                    .timestamp(Instant.now())
                    .build();

            rabbitTemplate.convertAndSend(RabbitMQConfig.SHIPPING_EXCHANGE, routingKey, shipmentEvent);
            log.info("Published {} for shipmentId={}", eventType, shipmentId);
        }

        return toResponse(shipment);
    }

    // ────────────────────────────────────────────────────────────────────────
    // Cron polling — syncs all shipments whose nextSyncAt is overdue
    // ────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public List<Map<String, Object>> pollDueShipments() {
        List<Shipment> due = shipmentRepository
                .findByAwbIsNotNullAndNextSyncAtBefore(LocalDateTime.now());

        log.info("[Poll] Found {} shipment(s) due for sync", due.size());

        List<Map<String, Object>> summary = new ArrayList<>();
        for (Shipment s : due) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("shipmentId", s.getId());
            result.put("awb",        s.getAwb());
            try {
                syncFromIthink(s);
                result.put("ok", true);
            } catch (Exception e) {
                log.error("[Poll] Sync failed for awb={}: {}", s.getAwb(), e.getMessage());
                result.put("ok",    false);
                result.put("error", e.getMessage());
            }
            summary.add(result);
        }
        return summary;
    }

    // ────────────────────────────────────────────────────────────────────────
    // Internal: pull latest tracking from iThink, upsert events, update status
    // ────────────────────────────────────────────────────────────────────────
    @Transactional
    protected Shipment syncFromIthink(Shipment shipment) {
        if (props.isSimulate()) {
            // Simulation: just bump nextSyncAt
            shipment.setLastSyncedAt(LocalDateTime.now());
            shipment.setNextSyncAt(LocalDateTime.now().plusMinutes(POLL_INTERVAL_MINUTES));
            return shipmentRepository.save(shipment);
        }

        Map<String, Object> itlRes = iThinkClient.trackOrder(shipment.getAwb());
        @SuppressWarnings("unchecked")
        Map<String, Object> trackData = (Map<String, Object>)
                ((Map<String, Object>) itlRes.getOrDefault("data", Map.of()))
                        .get(String.valueOf(shipment.getAwb()));

        // iThink call failed — bump and move on
        if (trackData == null || !"success".equals(trackData.get("message"))) {
            shipment.setLastSyncedAt(LocalDateTime.now());
            shipment.setNextSyncAt(LocalDateTime.now().plusMinutes(POLL_INTERVAL_MINUTES));
            return shipmentRepository.save(shipment);
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> odt = (Map<String, Object>) trackData.getOrDefault("order_date_time", Map.of());

        String itlStatus = (String) trackData.get("current_status");
        if (itlStatus != null) shipment.setIthinkStatus(itlStatus);
        if (trackData.get("logistic") != null) shipment.setCourierName((String) trackData.get("logistic"));

        // Parse iThink date strings to LocalDate
        String expDelivery = (String) odt.get("expected_delivery_date");
        String deliveredAt = (String) odt.get("delivery_date");
        if (expDelivery != null && !expDelivery.isBlank())
            shipment.setEstimatedDelivery(parseDate(expDelivery));
        if (deliveredAt != null && !deliveredAt.isBlank())
            shipment.setActualDelivery(parseDate(deliveredAt));

        // Map iThink status to internal enum
        ShipmentStatus mapped = FORWARD_STATUS_MAP.get(itlStatus);
        if (mapped != null) shipment.setStatus(mapped);

        shipment.setLastSyncedAt(LocalDateTime.now());
        shipment.setNextSyncAt(LocalDateTime.now().plusMinutes(POLL_INTERVAL_MINUTES));
        shipment = shipmentRepository.save(shipment);

        // Upsert scan events (dedup via uniqueKey)
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> scans = (List<Map<String, Object>>)
                trackData.getOrDefault("scan_details", List.of());

        for (Map<String, Object> scan : scans) {
            String scanTime   = (String) scan.get("scan_date_time");
            String scanStatus = (String) scan.get("status");
            String uniqueKey  = shipment.getAwb() + "::" + scanTime + "::" + scanStatus;

            if (trackingEventRepository.findByUniqueKey(uniqueKey).isEmpty()) {
                TrackingEvent te = TrackingEvent.builder()
                        .shipment(shipment)
                        .status(scanStatus)
                        .location((String) scan.get("scan_location"))
                        .description((String) scan.get("remark"))
                        .eventTime(parseDateTime(scanTime))
                        .uniqueKey(uniqueKey)
                        .build();
                trackingEventRepository.save(te);
            }
        }

        // Auto-publish events for DISPATCHED / DELIVERED transitions
        if (mapped == ShipmentStatus.DISPATCHED || mapped == ShipmentStatus.DELIVERED) {
            String eventType  = mapped == ShipmentStatus.DISPATCHED ? "SHIPMENT_DISPATCHED" : "SHIPMENT_DELIVERED";
            String routingKey = mapped == ShipmentStatus.DISPATCHED
                    ? RabbitMQConfig.SHIPMENT_DISPATCHED_KEY
                    : RabbitMQConfig.SHIPMENT_DELIVERED_KEY;

            ShipmentEvent event = ShipmentEvent.builder()
                    .eventType(eventType)
                    .shipmentId(shipment.getId())
                    .orderId(shipment.getOrderId())
                    .userId(shipment.getUserId())
                    .awb(shipment.getAwb())
                    .courierName(shipment.getCourierName())
                    .ithinkStatus(itlStatus)
                    .timestamp(Instant.now())
                    .build();
            rabbitTemplate.convertAndSend(RabbitMQConfig.SHIPPING_EXCHANGE, routingKey, event);
            log.info("[Sync] Published {} for awb={}", eventType, shipment.getAwb());
        }

        return shipment;
    }

    // ────────────────────────────────────────────────────────────────────────
    // Helpers
    // ────────────────────────────────────────────────────────────────────────

    private Shipment findOrThrow(UUID id) {
        return shipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found: " + id));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseAddressJson(String json) {
        if (json == null || json.isBlank()) return Map.of();
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(json, Map.class);
        } catch (Exception e) {
            log.warn("Failed to parse shippingAddress JSON: {}", e.getMessage());
            return Map.of();
        }
    }

    private LocalDate parseDate(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            // iThink returns dates in various formats — try common ones
            for (String pattern : new String[]{"yyyy-MM-dd", "dd-MM-yyyy", "MM/dd/yyyy"}) {
                try { return LocalDate.parse(s, DateTimeFormatter.ofPattern(pattern)); }
                catch (Exception ignored) {}
            }
        } catch (Exception e) {
            log.warn("Could not parse date '{}': {}", s, e.getMessage());
        }
        return null;
    }

    private LocalDateTime parseDateTime(String s) {
        if (s == null || s.isBlank()) return LocalDateTime.now();
        try {
            for (String pattern : new String[]{
                    "yyyy-MM-dd HH:mm:ss", "dd-MM-yyyy HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss"}) {
                try { return LocalDateTime.parse(s, DateTimeFormatter.ofPattern(pattern)); }
                catch (Exception ignored) {}
            }
        } catch (Exception e) {
            log.warn("Could not parse datetime '{}': {}", s, e.getMessage());
        }
        return LocalDateTime.now();
    }

    private ShipmentResponse toResponse(Shipment s) {
        List<ShipmentResponse.TrackingEventResponse> events =
                s.getTrackingEvents() == null ? List.of() :
                        s.getTrackingEvents().stream()
                                .map(e -> ShipmentResponse.TrackingEventResponse.builder()
                                        .status(e.getStatus())
                                        .location(e.getLocation())
                                        .description(e.getDescription())
                                        .eventTime(e.getEventTime())
                                        .build())
                                .toList();

        return ShipmentResponse.builder()
                .id(s.getId())
                .orderId(s.getOrderId())
                .userId(s.getUserId())
                .awb(s.getAwb())
                .courierName(s.getCourierName())
                .providerRef(s.getProviderRef())
                .labelUrl(s.getLabelUrl())
                .manifestUrl(s.getManifestUrl())
                .ithinkStatus(s.getIthinkStatus())
                .lastSyncedAt(s.getLastSyncedAt())
                .nextSyncAt(s.getNextSyncAt())
                .status(s.getStatus())
                .estimatedDelivery(s.getEstimatedDelivery())
                .actualDelivery(s.getActualDelivery())
                .shippingAddress(s.getShippingAddress())
                .trackingEvents(events)
                .createdAt(s.getCreatedAt())
                .build();
    }
}
