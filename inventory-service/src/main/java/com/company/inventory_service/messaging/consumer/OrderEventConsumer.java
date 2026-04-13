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
public class OrderEventConsumer {

    private final InventoryService inventoryService;

    @RabbitListener(queues = RabbitMQConfig.INV_ORDER_CANCELLED_QUEUE)
    public void onOrderCancelled(OrderEvent event) {
        log.info("Received ORDER_CANCELLED for orderId={}", event.getOrderId());
        inventoryService.releaseReservation(event.getOrderId());
    }
}
