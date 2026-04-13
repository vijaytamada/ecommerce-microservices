package com.company.product_service.document;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Document(collection = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    private String id;

    @Indexed
    private String name;

    private String description;

    @Indexed
    private String categoryId;

    private String categoryName;

    private UUID sellerId;              // userId from Auth/User Service

    private BigDecimal price;

    @Builder.Default
    private String currency = "USD";

    private List<String> images;        // URLs

    private Map<String, String> attributes;  // e.g. {"color": "red", "size": "L"}

    @Indexed
    @Builder.Default
    private ProductStatus status = ProductStatus.ACTIVE;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public enum ProductStatus {
        DRAFT, ACTIVE, INACTIVE
    }
}
