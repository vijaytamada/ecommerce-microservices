package com.company.inventory_service.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReserveRequest {

    @NotNull(message = "Order ID is required")
    private UUID orderId;

    @NotBlank(message = "Product ID is required")
    private String productId;

    @NotNull
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}
