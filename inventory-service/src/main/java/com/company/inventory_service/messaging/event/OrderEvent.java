package com.company.inventory_service.messaging.event;

import lombok.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class OrderEvent implements Serializable {
    private String eventType;
    private UUID orderId;
    private UUID userId;
    private Instant timestamp;
}
