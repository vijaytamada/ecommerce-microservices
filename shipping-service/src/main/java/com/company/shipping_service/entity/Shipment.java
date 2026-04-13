package com.company.shipping_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "shipments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "order_id", nullable = false, unique = true)
    private UUID orderId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "carrier", length = 50)
    @Builder.Default
    private String carrier = "LOCAL_COURIER";

    @Column(name = "tracking_number", unique = true)
    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private ShipmentStatus status = ShipmentStatus.PROCESSING;

    @Column(name = "estimated_delivery")
    private LocalDate estimatedDelivery;

    @Column(name = "actual_delivery")
    private LocalDate actualDelivery;

    @Column(name = "shipping_address", columnDefinition = "TEXT")
    private String shippingAddress;

    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TrackingEvent> trackingEvents;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum ShipmentStatus {
        PROCESSING, DISPATCHED, IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERED, FAILED
    }
}
