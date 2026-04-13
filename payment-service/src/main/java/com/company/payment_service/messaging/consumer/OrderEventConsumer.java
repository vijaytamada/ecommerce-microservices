package com.company.payment_service.messaging.consumer;

import com.company.payment_service.config.RabbitMQConfig;
import com.company.payment_service.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final PaymentService paymentService;

    @RabbitListener(queues = RabbitMQConfig.PAYMENT_ORDER_QUEUE)
    public void onOrderCreated(Map<String, Object> event) {
        log.info("Received ORDER_CREATED, processing payment for orderId={}", event.get("orderId"));
        paymentService.processPayment(event);
    }
}
