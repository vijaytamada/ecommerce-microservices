package com.company.auth_service.messaging.publisher;

import com.company.auth_service.config.RabbitMQConfig;
import com.company.auth_service.entity.User;
import com.company.auth_service.messaging.event.PasswordResetEvent;
import com.company.auth_service.messaging.event.UserEvent;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    /* ================= USER LIFECYCLE ================= */

    public void publishUserCreated(User user) {

        publishUserEvent("USER_CREATED", user,
                RabbitMQConfig.USER_CREATED_KEY);
    }


    public void publishUserUpdated(User user) {

        publishUserEvent("USER_UPDATED", user,
                RabbitMQConfig.USER_UPDATED_KEY);
    }


    public void publishUserDisabled(User user) {

        publishUserEvent("USER_DISABLED", user,
                RabbitMQConfig.USER_DISABLED_KEY);
    }


    /* ================= SECURITY EVENTS ================= */

    /* Password Changed */
    public void publishPasswordChanged(User user) {

        publishSecurityEvent(
                "PASSWORD_CHANGED",
                "user.security.password.changed",
                user
        );
    }


    /* Email Changed */
    public void publishEmailChanged(User user) {

        publishSecurityEvent(
                "EMAIL_CHANGED",
                "user.security.email.changed",
                user
        );
    }


    /* Email Verified */
    public void publishEmailVerified(User user) {

        publishSecurityEvent(
                "EMAIL_VERIFIED",
                "user.security.email.verified",
                user
        );
    }


    /* Password Reset Requested (Forgot Password) */
    public void publishPasswordResetRequested(
            User user,
            String token,
            Instant expiresAt
    ) {

        PasswordResetEvent event =
                PasswordResetEvent.builder()
                        .userId(user.getId())
                        .email(user.getEmail())
                        .token(token)
                        .expiresAt(expiresAt)
                        .build();

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.AUTH_EXCHANGE,
                "user.security.password.reset.requested",
                event
        );
    }


    /* Password Reset Success */
    public void publishPasswordResetSuccess(User user) {

        publishSecurityEvent(
                "PASSWORD_RESET_SUCCESS",
                "user.security.password.reset.success",
                user
        );
    }


    /* ================= COMMON HELPERS ================= */

    private void publishUserEvent(
            String type,
            User user,
            String routingKey
    ) {

        UserEvent event =
                UserEvent.builder()
                        .eventType(type)
                        .userId(user.getId())
                        .email(user.getEmail())
                        .timestamp(Instant.now())
                        .build();
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.AUTH_EXCHANGE,
                    routingKey,
                    event
            );
        } catch (AmqpException e) {
            throw new RuntimeException("RabbitMQ Failed!");
        }
    }


    private void publishSecurityEvent(
            String type,
            String routingKey,
            User user
    ) {

        UserEvent event =
                UserEvent.builder()
                        .eventType(type)
                        .userId(user.getId())
                        .email(user.getEmail())
                        .timestamp(Instant.now())
                        .build();

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.AUTH_EXCHANGE,
                    routingKey,
                    event
            );
        } catch (AmqpException e) {
            throw new RuntimeException("RabbitMQ Failed!");
        }
    }
}
