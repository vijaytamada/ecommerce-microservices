package com.company.product_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    /* ---- Exchange ---- */
    public static final String PRODUCT_EXCHANGE = "product.events";

    /* ---- Routing Keys ---- */
    public static final String PRODUCT_CREATED_KEY       = "product.created";
    public static final String PRODUCT_UPDATED_KEY       = "product.updated";
    public static final String PRODUCT_DELETED_KEY       = "product.deleted";
    public static final String PRODUCT_STATUS_CHANGED_KEY = "product.status.changed";

    @Bean
    public TopicExchange productExchange() {
        return new TopicExchange(PRODUCT_EXCHANGE, true, false);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
