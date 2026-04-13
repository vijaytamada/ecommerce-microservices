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

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/shipping")
@RequiredArgsConstructor
@Tag(name = "Shipping", description = "Shipment tracking and management APIs")
public class ShippingController {

    private final ShippingService shippingService;

    // ─── Public ───────────────────────────────────────────────────────────────

    @GetMapping("/check-pincode")
    @Operation(summary = "Check if a pincode is serviceable (public)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkPincode(@RequestParam String pincode) {
        return ResponseEntity.ok(ApiResponse.success("Pincode checked", shippingService.checkPincode(pincode)));
    }

    @GetMapping("/track/{awb}")
    @Operation(summary = "Track shipment by AWB number (public)")
    public ResponseEntity<ApiResponse<ShipmentResponse>> trackByAwb(@PathVariable String awb) {
        return ResponseEntity.ok(ApiResponse.success("Shipment tracked", shippingService.trackByNumber(awb)));
    }

    // ─── Customer ─────────────────────────────────────────────────────────────

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

    // ─── Admin ────────────────────────────────────────────────────────────────

    @PostMapping
    @Operation(summary = "Push a confirmed order to iThink and assign AWB (ADMIN)")
    public ResponseEntity<ApiResponse<ShipmentResponse>> createOnIthink(@RequestParam UUID orderId) {
        return ResponseEntity.ok(ApiResponse.success("Shipment created on iThink", shippingService.createShipmentOnIthink(orderId)));
    }

    @PostMapping("/{id}/track")
    @Operation(summary = "Force-sync latest tracking from iThink (ADMIN)")
    public ResponseEntity<ApiResponse<ShipmentResponse>> syncTracking(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Tracking synced", shippingService.syncTracking(id)));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel shipment on iThink (ADMIN)")
    public ResponseEntity<ApiResponse<ShipmentResponse>> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Shipment cancelled", shippingService.cancelShipmentOnIthink(id)));
    }

    @PostMapping("/{id}/documents")
    @Operation(summary = "Re-fetch label and manifest PDF URLs from iThink (ADMIN)")
    public ResponseEntity<ApiResponse<ShipmentResponse>> generateDocuments(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Documents generated", shippingService.generateDocuments(id)));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Manually update shipment status (ADMIN)")
    public ResponseEntity<ApiResponse<ShipmentResponse>> updateStatus(
            @PathVariable UUID id, @RequestParam ShipmentStatus status) {
        return ResponseEntity.ok(ApiResponse.success("Status updated", shippingService.updateStatus(id, status)));
    }

    // ─── Internal / Cron ──────────────────────────────────────────────────────

    @PostMapping("/poll/sync")
    @Operation(summary = "Poll all due shipments and sync tracking from iThink (INTERNAL/CRON)")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> pollSync() {
        return ResponseEntity.ok(ApiResponse.success("Poll completed", shippingService.pollDueShipments()));
    }
}
