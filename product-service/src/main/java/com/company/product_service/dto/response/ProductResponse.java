package com.company.product_service.dto.response;

import com.company.product_service.document.Product.ProductStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {

    private String id;
    private String name;
    private String description;
    private String categoryId;
    private String categoryName;
    private UUID sellerId;
    private BigDecimal price;
    private String currency;
    private List<String> images;
    private Map<String, String> attributes;
    private ProductStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
