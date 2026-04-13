package com.company.inventory_service.messaging.consumer;

import com.company.inventory_service.config.RabbitMQConfig;
import com.company.inventory_service.messaging.event.OrderEvent;
import com.company.inventory_service.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final InventoryService inventoryService;

    @RabbitListener(queues = RabbitMQConfig.INV_PAYMENT_EVENTS_QUEUE)
    public void onPaymentEvent(OrderEvent event) {
        if (event.getEventType() == null) return;
        switch (event.getEventType()) {
            case "PAYMENT_SUCCESS" -> {
                log.info("PAYMENT_SUCCESS received — confirming reservation for orderId={}", event.getOrderId());
                inventoryService.confirmReservation(event.getOrderId());
            }
            case "PAYMENT_FAILED" -> {
                log.info("PAYMENT_FAILED received — releasing reservation for orderId={}", event.getOrderId());
                inventoryService.releaseReservation(event.getOrderId());
            }
            default -> log.debug("Ignoring payment event type={}", event.getEventType());
        }
    }
}
