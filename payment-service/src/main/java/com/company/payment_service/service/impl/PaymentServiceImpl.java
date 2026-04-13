package com.company.payment_service.service.impl;

import com.company.payment_service.config.RabbitMQConfig;
import com.company.payment_service.dto.response.PaymentResponse;
import com.company.payment_service.entity.Payment;
import com.company.payment_service.entity.Payment.PaymentStatus;
import com.company.payment_service.exception.ResourceNotFoundException;
import com.company.payment_service.messaging.event.PaymentEvent;
import com.company.payment_service.repository.PaymentRepository;
import com.company.payment_service.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final RabbitTemplate rabbitTemplate;

    @Override
    @Transactional
    public PaymentResponse processPayment(Map<String, Object> orderEvent) {
        UUID orderId  = UUID.fromString((String) orderEvent.get("orderId"));
        UUID userId   = UUID.fromString((String) orderEvent.get("userId"));
        String email  = (String) orderEvent.get("userEmail");
        BigDecimal amt = new BigDecimal(orderEvent.get("totalAmount").toString());

        // Simulate payment: 90% success rate
        boolean success = Math.random() > 0.1;

        Payment payment = Payment.builder()
                .orderId(orderId)
                .userId(userId)
                .amount(amt)
                .status(success ? PaymentStatus.SUCCESS : PaymentStatus.FAILED)
                .transactionId(success ? "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase() : null)
                .failureReason(success ? null : "Simulated payment failure")
                .build();

        payment = paymentRepository.save(payment);

        // Publish payment event
        PaymentEvent event = PaymentEvent.builder()
                .eventType(success ? "PAYMENT_SUCCESS" : "PAYMENT_FAILED")
                .paymentId(payment.getId())
                .orderId(orderId)
                .userId(userId)
                .userEmail(email)
                .amount(amt)
                .currency(payment.getCurrency())
                .transactionId(payment.getTransactionId())
                .failureReason(payment.getFailureReason())
                .timestamp(Instant.now())
                .build();

        String routingKey = success ? RabbitMQConfig.PAYMENT_SUCCESS_KEY : RabbitMQConfig.PAYMENT_FAILED_KEY;
        rabbitTemplate.convertAndSend(RabbitMQConfig.PAYMENT_EXCHANGE, routingKey, event);
        log.info("Payment {} for orderId={}", success ? "SUCCESS" : "FAILED", orderId);

        return toResponse(payment);
    }

    @Override
    public PaymentResponse getPayment(UUID paymentId) {
        return toResponse(paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + paymentId)));
    }

    @Override
    public PaymentResponse getPaymentByOrder(UUID orderId) {
        return toResponse(paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order: " + orderId)));
    }

    @Override
    public Page<PaymentResponse> getMyPayments(UUID userId, Pageable pageable) {
        return paymentRepository.findByUserId(userId, pageable).map(this::toResponse);
    }

    @Override
    public Page<PaymentResponse> getAllPayments(Pageable pageable) {
        return paymentRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    @Transactional
    public PaymentResponse refund(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + paymentId));

        payment.setStatus(PaymentStatus.REFUNDED);
        payment = paymentRepository.save(payment);

        PaymentEvent event = PaymentEvent.builder()
                .eventType("PAYMENT_REFUNDED")
                .paymentId(payment.getId())
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .timestamp(Instant.now())
                .build();
        rabbitTemplate.convertAndSend(RabbitMQConfig.PAYMENT_EXCHANGE, RabbitMQConfig.PAYMENT_REFUNDED_KEY, event);

        return toResponse(payment);
    }

    private PaymentResponse toResponse(Payment p) {
        return PaymentResponse.builder()
                .id(p.getId()).orderId(p.getOrderId()).userId(p.getUserId())
                .amount(p.getAmount()).currency(p.getCurrency()).status(p.getStatus())
                .paymentMethod(p.getPaymentMethod()).transactionId(p.getTransactionId())
                .createdAt(p.getCreatedAt()).updatedAt(p.getUpdatedAt())
                .build();
    }
}
