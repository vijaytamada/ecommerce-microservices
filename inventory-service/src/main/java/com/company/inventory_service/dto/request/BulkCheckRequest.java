package com.company.inventory_service.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BulkCheckRequest {

    @NotEmpty(message = "Items list cannot be empty")
    private List<ReserveRequest> items;
}
