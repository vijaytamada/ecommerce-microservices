package com.company.shipping_service.repository;

import com.company.shipping_service.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShipmentRepository extends JpaRepository<Shipment, UUID> {
    Optional<Shipment> findByOrderId(UUID orderId);
    Optional<Shipment> findByAwb(String awb);

    /** Shipments whose nextSyncAt is overdue — used by the polling job. */
    List<Shipment> findByAwbIsNotNullAndNextSyncAtBefore(LocalDateTime threshold);
}
