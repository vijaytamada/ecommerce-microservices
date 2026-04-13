package com.company.shipping_service.service;

import com.company.shipping_service.dto.response.ShipmentResponse;
import com.company.shipping_service.entity.Shipment.ShipmentStatus;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ShippingService {

    /** Called automatically from order.confirmed RabbitMQ event. Creates the DB record. */
    ShipmentResponse createShipment(Map<String, Object> orderEvent);

    /** Admin: push a confirmed order to iThink and get AWB assigned. */
    ShipmentResponse createShipmentOnIthink(UUID orderId);

    /** Check if a pincode is serviceable by iThink (public). */
    Map<String, Object> checkPincode(String pincode);

    ShipmentResponse getShipmentByOrder(UUID orderId);
    ShipmentResponse getShipment(UUID shipmentId);

    /** Public tracking by AWB number. */
    ShipmentResponse trackByNumber(String awb);

    /** Admin: force-sync latest tracking from iThink right now. */
    ShipmentResponse syncTracking(UUID shipmentId);

    /** Admin: cancel the shipment on iThink. */
    ShipmentResponse cancelShipmentOnIthink(UUID shipmentId);

    /** Admin: re-fetch label & manifest PDF URLs from iThink. */
    ShipmentResponse generateDocuments(UUID shipmentId);

    /** Admin/manual status update (also publishes RabbitMQ events for DELIVERED/DISPATCHED). */
    ShipmentResponse updateStatus(UUID shipmentId, ShipmentStatus status);

    /** Cron/internal: polls all shipments whose nextSyncAt is overdue. */
    List<Map<String, Object>> pollDueShipments();
}
