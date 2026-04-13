package com.company.shipping_service.messaging.event;

import lombok.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class ShipmentEvent implements Serializable {
    private String eventType;     // SHIPMENT_DISPATCHED, SHIPMENT_DELIVERED
    private UUID   shipmentId;
    private UUID   orderId;
    private UUID   userId;
    private String awb;           // iThink AWB number (also used as tracking number)
    private String courierName;
    private String ithinkStatus;
    private Instant timestamp;
}
