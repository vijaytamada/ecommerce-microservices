package com.company.review_service.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReviewRequest {

    @NotBlank(message = "Product ID is required")
    private String productId;

    @NotNull(message = "Order ID is required")
    private UUID orderId;

    @Min(1) @Max(5)
    @NotNull(message = "Rating is required (1-5)")
    private Integer rating;

    @Size(max = 150)
    private String title;

    @Size(max = 2000)
    private String body;
}
