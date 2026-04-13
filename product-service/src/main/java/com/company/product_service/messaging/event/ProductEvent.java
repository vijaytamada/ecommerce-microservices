package com.company.product_service.messaging.event;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductEvent implements Serializable {

    private String eventType;       // PRODUCT_CREATED, PRODUCT_UPDATED, PRODUCT_DELETED, PRODUCT_STATUS_CHANGED
    private String productId;
    private String productName;
    private String categoryId;
    private String categoryName;
    private BigDecimal price;
    private String status;
    private Instant timestamp;
}
