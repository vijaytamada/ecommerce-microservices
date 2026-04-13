package com.company.review_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String ORDER_EXCHANGE  = "order.events";
    public static final String REVIEW_EXCHANGE = "review.events";

    public static final String REVIEW_SUBMITTED_KEY = "review.submitted";

    public static final String REVIEW_ORDER_DELIVERED_QUEUE = "review.order.delivered.q";

    @Bean public TopicExchange orderExchange()  { return new TopicExchange(ORDER_EXCHANGE, true, false); }
    @Bean public TopicExchange reviewExchange() { return new TopicExchange(REVIEW_EXCHANGE, true, false); }

    @Bean public Queue reviewOrderDeliveredQueue() { return QueueBuilder.durable(REVIEW_ORDER_DELIVERED_QUEUE).build(); }

    @Bean
    public Binding reviewOrderDeliveredBinding() {
        return BindingBuilder.bind(reviewOrderDeliveredQueue()).to(orderExchange()).with("order.delivered");
    }

    @Bean public Jackson2JsonMessageConverter messageConverter() { return new Jackson2JsonMessageConverter(); }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf) {
        RabbitTemplate t = new RabbitTemplate(cf);
        t.setMessageConverter(messageConverter());
        return t;
    }
}
