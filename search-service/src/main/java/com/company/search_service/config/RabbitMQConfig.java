package com.company.search_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String PRODUCT_EXCHANGE = "product.events";
    public static final String REVIEW_EXCHANGE  = "review.events";

    public static final String SEARCH_PRODUCT_QUEUE = "search.product.events.q";
    public static final String SEARCH_REVIEW_QUEUE  = "search.review.events.q";

    @Bean public TopicExchange productExchange() { return new TopicExchange(PRODUCT_EXCHANGE, true, false); }
    @Bean public TopicExchange reviewExchange()  { return new TopicExchange(REVIEW_EXCHANGE, true, false); }

    @Bean public Queue searchProductQueue() { return QueueBuilder.durable(SEARCH_PRODUCT_QUEUE).build(); }
    @Bean public Queue searchReviewQueue()  { return QueueBuilder.durable(SEARCH_REVIEW_QUEUE).build(); }

    @Bean
    public Binding searchProductBinding() {
        return BindingBuilder.bind(searchProductQueue()).to(productExchange()).with("product.#");
    }

    @Bean
    public Binding searchReviewBinding() {
        return BindingBuilder.bind(searchReviewQueue()).to(reviewExchange()).with("review.submitted");
    }

    @Bean public Jackson2JsonMessageConverter messageConverter() { return new Jackson2JsonMessageConverter(); }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf) {
        RabbitTemplate t = new RabbitTemplate(cf);
        t.setMessageConverter(messageConverter());
        return t;
    }
}
