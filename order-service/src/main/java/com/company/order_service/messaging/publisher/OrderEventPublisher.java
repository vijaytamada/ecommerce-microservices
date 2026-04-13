package com.company.order_service.messaging.publisher;

import com.company.order_service.config.RabbitMQConfig;
import com.company.order_service.entity.Order;
import com.company.order_service.messaging.event.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishOrderCreated(OrderEvent event) {
        publish(event, RabbitMQConfig.ORDER_CREATED_KEY);
    }

    public void publishOrderConfirmed(OrderEvent event) {
        publish(event, RabbitMQConfig.ORDER_CONFIRMED_KEY);
    }

    public void publishOrderCancelled(OrderEvent event) {
        publish(event, RabbitMQConfig.ORDER_CANCELLED_KEY);
    }

    public void publishOrderShipped(OrderEvent event) {
        publish(event, RabbitMQConfig.ORDER_SHIPPED_KEY);
    }

    public void publishOrderDelivered(OrderEvent event) {
        publish(event, RabbitMQConfig.ORDER_DELIVERED_KEY);
    }

    private void publish(OrderEvent event, String routingKey) {
        log.info("Publishing {} for orderId={}", event.getEventType(), event.getOrderId());
        rabbitTemplate.convertAndSend(RabbitMQConfig.ORDER_EXCHANGE, routingKey, event);
    }
}
