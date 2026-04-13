package com.company.order_service.messaging.event;

import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class OrderEvent implements Serializable {
    private String eventType;   // ORDER_CREATED, ORDER_CONFIRMED, ORDER_CANCELLED, ORDER_SHIPPED, ORDER_DELIVERED
    private UUID orderId;
    private UUID userId;
    private String userEmail;
    private BigDecimal totalAmount;
    private String currency;
    private List<OrderItemInfo> items;
    private String shippingAddress;
    private Instant timestamp;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class OrderItemInfo implements Serializable {
        private String productId;
        private String productName;
        private int quantity;
        private BigDecimal unitPrice;
    }
}
