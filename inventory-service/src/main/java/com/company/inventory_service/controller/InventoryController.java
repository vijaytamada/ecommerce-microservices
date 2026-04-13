package com.company.inventory_service.controller;

import com.company.inventory_service.dto.request.BulkCheckRequest;
import com.company.inventory_service.dto.request.ReserveRequest;
import com.company.inventory_service.dto.request.StockRequest;
import com.company.inventory_service.dto.response.ApiResponse;
import com.company.inventory_service.dto.response.BulkAvailabilityResponse;
import com.company.inventory_service.dto.response.InventoryResponse;
import com.company.inventory_service.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Stock management APIs")
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    @Operation(summary = "Create inventory entry for a product")
    public ResponseEntity<ApiResponse<InventoryResponse>> createStock(@Valid @RequestBody StockRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Inventory created", inventoryService.createStock(request)));
    }

    @GetMapping("/{productId}")
    @Operation(summary = "Get stock level for a product")
    public ResponseEntity<ApiResponse<InventoryResponse>> getStock(@PathVariable String productId) {
        return ResponseEntity.ok(ApiResponse.success("Stock fetched", inventoryService.getStock(productId)));
    }

    @PutMapping("/{productId}/restock")
    @Operation(summary = "Add stock quantity (restock)")
    public ResponseEntity<ApiResponse<InventoryResponse>> restock(
            @PathVariable String productId, @RequestParam int quantity) {
        return ResponseEntity.ok(ApiResponse.success("Stock updated", inventoryService.updateStock(productId, quantity)));
    }

    @GetMapping("/low-stock")
    @Operation(summary = "List all low-stock items (ADMIN)")
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getLowStock() {
        return ResponseEntity.ok(ApiResponse.success("Low stock items fetched", inventoryService.getLowStockItems()));
    }

    @PostMapping("/bulk-check")
    @Operation(summary = "Check availability for multiple products")
    public ResponseEntity<ApiResponse<BulkAvailabilityResponse>> bulkCheck(@Valid @RequestBody BulkCheckRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Availability checked", inventoryService.bulkCheck(request)));
    }

    @PostMapping("/reserve")
    @Operation(summary = "Reserve stock for an order item")
    public ResponseEntity<ApiResponse<Void>> reserve(@Valid @RequestBody ReserveRequest request) {
        inventoryService.reserve(request);
        return ResponseEntity.ok(ApiResponse.success("Stock reserved", null));
    }

    @PostMapping("/confirm/{orderId}")
    @Operation(summary = "Confirm stock reservation after payment")
    public ResponseEntity<ApiResponse<Void>> confirm(@PathVariable UUID orderId) {
        inventoryService.confirmReservation(orderId);
        return ResponseEntity.ok(ApiResponse.success("Reservation confirmed", null));
    }

    @PostMapping("/release/{orderId}")
    @Operation(summary = "Release stock reservation (order cancelled)")
    public ResponseEntity<ApiResponse<Void>> release(@PathVariable UUID orderId) {
        inventoryService.releaseReservation(orderId);
        return ResponseEntity.ok(ApiResponse.success("Reservation released", null));
    }
}
