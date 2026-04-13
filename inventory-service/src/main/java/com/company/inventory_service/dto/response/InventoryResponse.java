package com.company.inventory_service.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InventoryResponse {
    private String productId;
    private int quantityAvailable;
    private int quantityReserved;
    private int lowStockThreshold;
    private boolean inStock;
    private LocalDateTime updatedAt;
}
