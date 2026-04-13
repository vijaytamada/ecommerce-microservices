package com.company.shipping_service.repository;

import com.company.shipping_service.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ShipmentRepository extends JpaRepository<Shipment, UUID> {
    Optional<Shipment> findByOrderId(UUID orderId);
    Optional<Shipment> findByTrackingNumber(String trackingNumber);
}
