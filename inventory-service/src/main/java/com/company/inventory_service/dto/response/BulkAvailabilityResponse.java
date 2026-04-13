package com.company.inventory_service.dto.response;

import lombok.*;
import java.util.Map;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BulkAvailabilityResponse {
    private boolean allAvailable;
    private Map<String, Boolean> availability;  // productId -> available
}
