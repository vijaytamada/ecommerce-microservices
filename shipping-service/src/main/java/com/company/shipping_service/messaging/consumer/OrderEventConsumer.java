package com.company.shipping_service.messaging.consumer;

import com.company.shipping_service.config.RabbitMQConfig;
import com.company.shipping_service.service.ShippingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final ShippingService shippingService;

    @RabbitListener(queues = RabbitMQConfig.SHIPPING_ORDER_CONFIRMED_QUEUE)
    public void onOrderConfirmed(Map<String, Object> event) {
        log.info("Received ORDER_CONFIRMED, creating shipment for orderId={}", event.get("orderId"));
        shippingService.createShipment(event);
    }
}
