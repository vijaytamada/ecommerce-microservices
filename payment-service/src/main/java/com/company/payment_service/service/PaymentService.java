package com.company.payment_service.service;

import com.company.payment_service.dto.response.PaymentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.UUID;

public interface PaymentService {
    PaymentResponse processPayment(Map<String, Object> orderEvent);
    PaymentResponse getPayment(UUID paymentId);
    PaymentResponse getPaymentByOrder(UUID orderId);
    Page<PaymentResponse> getMyPayments(UUID userId, Pageable pageable);
    Page<PaymentResponse> getAllPayments(Pageable pageable);
    PaymentResponse refund(UUID paymentId);
}
