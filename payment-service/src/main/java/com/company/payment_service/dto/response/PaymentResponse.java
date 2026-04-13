package com.company.payment_service.dto.response;

import com.company.payment_service.entity.Payment.PaymentMethod;
import com.company.payment_service.entity.Payment.PaymentStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentResponse {
    private UUID id;
    private UUID orderId;
    private UUID userId;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private PaymentMethod paymentMethod;
    private String transactionId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
