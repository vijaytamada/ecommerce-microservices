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

    // ── iThink fields ─────────────────────────────────────────────────────────

    /** AWB (Air Waybill) number assigned by iThink. Also serves as the tracking number. */
    @Column(name = "awb", unique = true)
    private String awb;

    /** Courier / logistics provider name returned by iThink (e.g. "Delhivery"). */
    @Column(name = "courier_name", length = 100)
    private String courierName;

    /** iThink internal reference number (refnum). */
    @Column(name = "provider_ref", length = 100)
    private String providerRef;

    /** Shipping label PDF URL returned by iThink. */
    @Column(name = "label_url", columnDefinition = "TEXT")
    private String labelUrl;

    /** Manifest PDF URL returned by iThink. */
    @Column(name = "manifest_url", columnDefinition = "TEXT")
    private String manifestUrl;

    /**
     * Raw current_status string from iThink (e.g. "Picked Up", "Delivered", "RTO Pending").
     * Stored as-is — no mapping applied.
     */
    @Column(name = "ithink_status", length = 100)
    @Builder.Default
    private String ithinkStatus = "Manifested";

    /** When we last polled iThink for tracking updates. */
    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    /** When to next poll iThink (set to now + 2h after each sync). */
    @Column(name = "next_sync_at")
    private LocalDateTime nextSyncAt;

    // ── Core fields ───────────────────────────────────────────────────────────

    /** Internal status enum for the order lifecycle. */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private ShipmentStatus status = ShipmentStatus.PROCESSING;

    @Column(name = "estimated_delivery")
    private LocalDate estimatedDelivery;

    @Column(name = "actual_delivery")
    private LocalDate actualDelivery;

    /** Snapshot of the shipping address JSON at order time. */
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

    // ── Status enum ───────────────────────────────────────────────────────────

    public enum ShipmentStatus {
        PROCESSING,         // Shipment record created, awaiting iThink AWB assignment
        MANIFESTED,         // AWB assigned, label generated
        DISPATCHED,         // Picked up by courier
        IN_TRANSIT,         // Moving between hubs
        OUT_FOR_DELIVERY,   // Last-mile delivery
        DELIVERED,          // Successfully delivered
        RTO,                // Return to origin initiated
        FAILED,             // Delivery failed / cancelled
        CANCELLED
    }
}
