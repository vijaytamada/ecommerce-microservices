package com.company.order_service.messaging.consumer;

import com.company.order_service.config.RabbitMQConfig;
import com.company.order_service.entity.Order.OrderStatus;
import com.company.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final OrderService orderService;

    @RabbitListener(queues = RabbitMQConfig.ORDER_PAYMENT_QUEUE)
    public void onPaymentEvent(Map<String, Object> event) {
        String type = (String) event.get("eventType");
        String orderIdStr = (String) event.get("orderId");
        if (type == null || orderIdStr == null) return;

        UUID orderId = UUID.fromString(orderIdStr);
        switch (type) {
            case "PAYMENT_SUCCESS" -> {
                log.info("PAYMENT_SUCCESS — confirming order {}", orderId);
                orderService.updateStatus(orderId, OrderStatus.CONFIRMED);
            }
            case "PAYMENT_FAILED" -> {
                log.info("PAYMENT_FAILED — cancelling order {}", orderId);
                orderService.updateStatus(orderId, OrderStatus.CANCELLED);
            }
            default -> log.debug("Ignoring payment event: {}", type);
        }
    }
}
