package com.company.payment_service.messaging.event;

import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class PaymentEvent implements Serializable {
    private String eventType;   // PAYMENT_SUCCESS, PAYMENT_FAILED, PAYMENT_REFUNDED
    private UUID paymentId;
    private UUID orderId;
    private UUID userId;
    private String userEmail;
    private BigDecimal amount;
    private String currency;
    private String transactionId;
    private String failureReason;
    private Instant timestamp;
}
