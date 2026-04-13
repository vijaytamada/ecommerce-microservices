package com.company.shipping_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tracking_events")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TrackingEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "location")
    private String location;

    @Column(name = "description")
    private String description;

    @CreationTimestamp
    @Column(name = "event_time", updatable = false)
    private LocalDateTime eventTime;
}
