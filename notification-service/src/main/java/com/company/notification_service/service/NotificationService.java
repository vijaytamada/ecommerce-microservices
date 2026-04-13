package com.company.notification_service.service;

import com.company.notification_service.document.Notification;

import java.util.UUID;

public interface NotificationService {
    void sendAndSave(UUID userId, String email, String type, String subject, String body);
}
