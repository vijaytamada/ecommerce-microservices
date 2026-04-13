package com.company.shipping_service.service;

import com.company.shipping_service.dto.response.ShipmentResponse;
import com.company.shipping_service.entity.Shipment.ShipmentStatus;

import java.util.Map;
import java.util.UUID;

public interface ShippingService {
    ShipmentResponse createShipment(Map<String, Object> orderEvent);
    ShipmentResponse getShipmentByOrder(UUID orderId);
    ShipmentResponse getShipment(UUID shipmentId);
    ShipmentResponse trackByNumber(String trackingNumber);
    ShipmentResponse updateStatus(UUID shipmentId, ShipmentStatus status);
}
