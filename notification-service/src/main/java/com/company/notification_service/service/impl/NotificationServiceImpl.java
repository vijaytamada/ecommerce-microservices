package com.company.notification_service.service.impl;

import com.company.notification_service.document.Notification;
import com.company.notification_service.repository.NotificationRepository;
import com.company.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final JavaMailSender mailSender;

    @Override
    public void sendAndSave(UUID userId, String email, String type, String subject, String body) {
        String status = "SENT";

        // Try sending email
        try {
            if (email != null && !email.isBlank()) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(email);
                message.setSubject(subject);
                message.setText(body);
                mailSender.send(message);
                log.info("Email sent to {} for event={}", email, type);
            }
        } catch (Exception e) {
            log.error("Failed to send email to {} for event={}: {}", email, type, e.getMessage());
            status = "FAILED";
        }

        // Always save to DB for audit/history
        Notification notification = Notification.builder()
                .userId(userId)
                .email(email)
                .type(type)
                .subject(subject)
                .body(body)
                .status(status)
                .build();

        notificationRepository.save(notification);
    }
}
