package com.company.order_service.dto.response;

import com.company.order_service.entity.Order.OrderStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderResponse {
    private UUID id;
    private UUID userId;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String currency;
    private String shippingAddress;
    private List<OrderItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class OrderItemResponse {
        private Long id;
        private String productId;
        private String productName;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
    }
}
