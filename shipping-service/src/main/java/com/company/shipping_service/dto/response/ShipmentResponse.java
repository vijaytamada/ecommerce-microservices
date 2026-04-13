package com.company.shipping_service.dto.response;

import com.company.shipping_service.entity.Shipment.ShipmentStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ShipmentResponse {

    private UUID id;
    private UUID orderId;
    private UUID userId;

    // iThink fields
    private String awb;
    private String courierName;
    private String providerRef;
    private String labelUrl;
    private String manifestUrl;
    private String ithinkStatus;
    private LocalDateTime lastSyncedAt;
    private LocalDateTime nextSyncAt;

    // Core fields
    private ShipmentStatus status;
    private LocalDate estimatedDelivery;
    private LocalDate actualDelivery;
    private String shippingAddress;

    private List<TrackingEventResponse> trackingEvents;
    private LocalDateTime createdAt;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class TrackingEventResponse {
        private String status;
        private String location;
        private String description;
        private LocalDateTime eventTime;
    }
}
