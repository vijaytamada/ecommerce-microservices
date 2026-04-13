package com.company.shipping_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tracking_events",
       uniqueConstraints = @UniqueConstraint(name = "uq_tracking_event_key", columnNames = "unique_key"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TrackingEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    /** iThink scan status (e.g. "Picked Up", "In Transit", "Delivered"). */
    @Column(name = "status", nullable = false)
    private String status;

    /** Scan location from iThink (scan_location field). */
    @Column(name = "location")
    private String location;

    /** Human-readable description / remark from iThink (remark field). */
    @Column(name = "description")
    private String description;

    /** Exact time of this scan event (scan_date_time from iThink). */
    @Column(name = "event_time", nullable = false)
    private LocalDateTime eventTime;

    /**
     * Deduplication key: "{awb}::{scan_date_time}::{status}".
     * Used for idempotent upsert so re-polling does not create duplicate events.
     */
    @Column(name = "unique_key", length = 255)
    private String uniqueKey;
}
