package com.company.notification_service.document;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "notifications")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {

    @Id
    private String id;

    @Indexed
    private UUID userId;

    private String email;
    private String type;        // ORDER_CONFIRMED, PAYMENT_SUCCESS, etc.
    private String subject;
    private String body;

    @Builder.Default
    private String channel = "EMAIL";

    @Builder.Default
    private String status = "SENT";  // SENT, FAILED

    @CreatedDate
    private LocalDateTime createdAt;
}
