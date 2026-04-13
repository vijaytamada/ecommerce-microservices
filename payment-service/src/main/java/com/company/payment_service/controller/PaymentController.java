package com.company.payment_service.controller;

import com.company.payment_service.dto.response.ApiResponse;
import com.company.payment_service.dto.response.PaymentResponse;
import com.company.payment_service.service.PaymentService;
import com.company.payment_service.utils.HeaderUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment management APIs")
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/{id}")
    @Operation(summary = "Get payment by ID")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Payment fetched", paymentService.getPayment(id)));
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get payment by order ID")
    public ResponseEntity<ApiResponse<PaymentResponse>> getByOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok(ApiResponse.success("Payment fetched", paymentService.getPaymentByOrder(orderId)));
    }

    @GetMapping("/my")
    @Operation(summary = "My payment history")
    public ResponseEntity<ApiResponse<Page<PaymentResponse>>> getMyPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        UUID userId = HeaderUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Payments fetched",
                paymentService.getMyPayments(userId, PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    @GetMapping("/admin/all")
    @Operation(summary = "All payments (ADMIN)")
    public ResponseEntity<ApiResponse<Page<PaymentResponse>>> getAllPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success("All payments fetched",
                paymentService.getAllPayments(PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    @PostMapping("/{id}/refund")
    @Operation(summary = "Refund a payment (ADMIN)")
    public ResponseEntity<ApiResponse<PaymentResponse>> refund(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Refund initiated", paymentService.refund(id)));
    }
}
