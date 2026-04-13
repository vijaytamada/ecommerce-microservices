package com.company.inventory_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InventoryItem {

    @Id
    @Column(name = "product_id", nullable = false, updatable = false)
    private String productId;

    @Column(name = "quantity_available", nullable = false)
    @Builder.Default
    private int quantityAvailable = 0;

    @Column(name = "quantity_reserved", nullable = false)
    @Builder.Default
    private int quantityReserved = 0;

    @Column(name = "low_stock_threshold", nullable = false)
    @Builder.Default
    private int lowStockThreshold = 5;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
