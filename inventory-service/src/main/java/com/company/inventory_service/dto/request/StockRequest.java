package com.company.inventory_service.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StockRequest {

    @NotBlank(message = "Product ID is required")
    private String productId;

    @NotNull
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;

    @Min(value = 0)
    private Integer lowStockThreshold;
}
