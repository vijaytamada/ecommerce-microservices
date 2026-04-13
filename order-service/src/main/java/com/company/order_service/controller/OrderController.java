package com.company.order_service.controller;

import com.company.order_service.dto.request.PlaceOrderRequest;
import com.company.order_service.dto.response.ApiResponse;
import com.company.order_service.dto.response.OrderResponse;
import com.company.order_service.entity.Order.OrderStatus;
import com.company.order_service.service.OrderService;
import com.company.order_service.utils.HeaderUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management APIs")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Place a new order")
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(@Valid @RequestBody PlaceOrderRequest request) {
        UUID userId = HeaderUtils.getCurrentUserId();
        String email = HeaderUtils.getCurrentUserEmail();
        return ResponseEntity.ok(ApiResponse.success("Order placed", orderService.placeOrder(userId, email, request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Order fetched", orderService.getOrder(id)));
    }

    @GetMapping
    @Operation(summary = "Get my orders")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getMyOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        UUID userId = HeaderUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Orders fetched",
                orderService.getMyOrders(userId, PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    @DeleteMapping("/{id}/cancel")
    @Operation(summary = "Cancel an order")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(@PathVariable UUID id) {
        UUID userId = HeaderUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Order cancelled", orderService.cancelOrder(id, userId)));
    }

    @GetMapping("/admin/all")
    @Operation(summary = "List all orders (ADMIN)")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success("All orders fetched",
                orderService.getAllOrders(PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update order status (ADMIN/internal)")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable UUID id, @RequestParam OrderStatus status) {
        return ResponseEntity.ok(ApiResponse.success("Status updated", orderService.updateStatus(id, status)));
    }
}
