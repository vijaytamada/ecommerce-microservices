package com.company.order_service.client;

import com.company.order_service.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@FeignClient(name = "inventory-service")
public interface InventoryClient {

    @PostMapping("/api/inventory/bulk-check")
    ApiResponse<Map<String, Object>> bulkCheck(@RequestBody Map<String, Object> request);

    @PostMapping("/api/inventory/reserve")
    ApiResponse<Void> reserve(@RequestBody Map<String, Object> request);
}
