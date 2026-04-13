package com.company.product_service.messaging.publisher;

import com.company.product_service.config.RabbitMQConfig;
import com.company.product_service.document.Product;
import com.company.product_service.messaging.event.ProductEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishProductCreated(Product product) {
        publish(buildEvent("PRODUCT_CREATED", product), RabbitMQConfig.PRODUCT_CREATED_KEY);
    }

    public void publishProductUpdated(Product product) {
        publish(buildEvent("PRODUCT_UPDATED", product), RabbitMQConfig.PRODUCT_UPDATED_KEY);
    }

    public void publishProductDeleted(String productId) {
        ProductEvent event = ProductEvent.builder()
                .eventType("PRODUCT_DELETED")
                .productId(productId)
                .timestamp(Instant.now())
                .build();
        publish(event, RabbitMQConfig.PRODUCT_DELETED_KEY);
    }

    public void publishStatusChanged(Product product) {
        publish(buildEvent("PRODUCT_STATUS_CHANGED", product), RabbitMQConfig.PRODUCT_STATUS_CHANGED_KEY);
    }

    private void publish(ProductEvent event, String routingKey) {
        log.info("Publishing {} for productId={}", event.getEventType(), event.getProductId());
        rabbitTemplate.convertAndSend(RabbitMQConfig.PRODUCT_EXCHANGE, routingKey, event);
    }

    private ProductEvent buildEvent(String type, Product product) {
        return ProductEvent.builder()
                .eventType(type)
                .productId(product.getId())
                .productName(product.getName())
                .categoryId(product.getCategoryId())
                .categoryName(product.getCategoryName())
                .price(product.getPrice())
                .status(product.getStatus().name())
                .timestamp(Instant.now())
                .build();
    }
}
