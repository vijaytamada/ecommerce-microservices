package com.company.inventory_service.messaging.event;

import lombok.*;
import java.io.Serializable;
import java.time.Instant;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class InventoryEvent implements Serializable {
    private String eventType;   // INVENTORY_LOW_STOCK
    private String productId;
    private int quantityAvailable;
    private int threshold;
    private Instant timestamp;
}
