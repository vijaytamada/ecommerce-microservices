package com.company.search_service.messaging.consumer;

import com.company.search_service.config.RabbitMQConfig;
import com.company.search_service.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventConsumer {

    private final SearchService searchService;

    @RabbitListener(queues = RabbitMQConfig.SEARCH_PRODUCT_QUEUE)
    public void onProductEvent(Map<String, Object> event) {
        log.info("Received product event: {}", event.get("eventType"));
        searchService.indexProduct(event);
    }

    @RabbitListener(queues = RabbitMQConfig.SEARCH_REVIEW_QUEUE)
    public void onReviewSubmitted(Map<String, Object> event) {
        String productId = (String) event.get("productId");
        // In a full implementation: recalculate avg from review-service
        // For now: just log and update incrementally
        log.info("Review submitted for productId={}, triggering rating update", productId);
    }
}
