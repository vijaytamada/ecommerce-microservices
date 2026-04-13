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
public class ShippingEventConsumer {

    private final OrderService orderService;

    @RabbitListener(queues = RabbitMQConfig.ORDER_SHIPPING_QUEUE)
    public void onShippingEvent(Map<String, Object> event) {
        String type = (String) event.get("eventType");
        String orderIdStr = (String) event.get("orderId");
        if (type == null || orderIdStr == null) return;

        UUID orderId = UUID.fromString(orderIdStr);
        switch (type) {
            case "SHIPMENT_DISPATCHED" -> {
                log.info("SHIPMENT_DISPATCHED — marking order {} as SHIPPED", orderId);
                orderService.updateStatus(orderId, OrderStatus.SHIPPED);
            }
            case "SHIPMENT_DELIVERED" -> {
                log.info("SHIPMENT_DELIVERED — marking order {} as DELIVERED", orderId);
                orderService.updateStatus(orderId, OrderStatus.DELIVERED);
            }
            default -> log.debug("Ignoring shipping event: {}", type);
        }
    }
}
