package com.company.notification_service.repository;

import com.company.notification_service.document.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface NotificationRepository extends MongoRepository<Notification, String> {
    Page<Notification> findByUserId(UUID userId, Pageable pageable);
}
