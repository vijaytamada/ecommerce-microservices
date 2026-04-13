package com.company.shipping_service.repository;

import com.company.shipping_service.entity.TrackingEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrackingEventRepository extends JpaRepository<TrackingEvent, Long> {
    Optional<TrackingEvent> findByUniqueKey(String uniqueKey);
}
