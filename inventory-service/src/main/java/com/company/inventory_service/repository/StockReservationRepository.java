package com.company.inventory_service.repository;

import com.company.inventory_service.entity.StockReservation;
import com.company.inventory_service.entity.StockReservation.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockReservationRepository extends JpaRepository<StockReservation, Long> {
    List<StockReservation> findByOrderIdAndStatus(UUID orderId, ReservationStatus status);
    Optional<StockReservation> findByOrderIdAndProductId(UUID orderId, String productId);
}
