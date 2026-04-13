package com.company.shipping_service.controller;

import com.company.shipping_service.dto.response.ApiResponse;
import com.company.shipping_service.dto.response.ShipmentResponse;
import com.company.shipping_service.entity.Shipment.ShipmentStatus;
import com.company.shipping_service.service.ShippingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/shipping")
@RequiredArgsConstructor
@Tag(name = "Shipping", description = "Shipment tracking and management APIs")
public class ShippingController {

    private final ShippingService shippingService;

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get shipment for an order")
    public ResponseEntity<ApiResponse<ShipmentResponse>> getByOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok(ApiResponse.success("Shipment fetched", shippingService.getShipmentByOrder(orderId)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get shipment by ID")
    public ResponseEntity<ApiResponse<ShipmentResponse>> getShipment(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Shipment fetched", shippingService.getShipment(id)));
    }

    @GetMapping("/track/{trackingNumber}")
    @Operation(summary = "Track shipment by tracking number (public)")
    public ResponseEntity<ApiResponse<ShipmentResponse>> track(@PathVariable String trackingNumber) {
        return ResponseEntity.ok(ApiResponse.success("Shipment tracked", shippingService.trackByNumber(trackingNumber)));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update shipment status (ADMIN)")
    public ResponseEntity<ApiResponse<ShipmentResponse>> updateStatus(
            @PathVariable UUID id, @RequestParam ShipmentStatus status) {
        return ResponseEntity.ok(ApiResponse.success("Status updated", shippingService.updateStatus(id, status)));
    }
}
