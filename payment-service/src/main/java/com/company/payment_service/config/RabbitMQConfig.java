package com.company.payment_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String PAYMENT_EXCHANGE = "payment.events";
    public static final String ORDER_EXCHANGE   = "order.events";

    public static final String PAYMENT_SUCCESS_KEY  = "payment.success";
    public static final String PAYMENT_FAILED_KEY   = "payment.failed";
    public static final String PAYMENT_REFUNDED_KEY = "payment.refunded";

    public static final String PAYMENT_ORDER_QUEUE = "payment.order.created.q";

    @Bean public TopicExchange paymentExchange() { return new TopicExchange(PAYMENT_EXCHANGE, true, false); }
    @Bean public TopicExchange orderExchange()   { return new TopicExchange(ORDER_EXCHANGE, true, false); }

    @Bean public Queue paymentOrderQueue() { return QueueBuilder.durable(PAYMENT_ORDER_QUEUE).build(); }

    @Bean
    public Binding paymentOrderBinding() {
        return BindingBuilder.bind(paymentOrderQueue()).to(orderExchange()).with("order.created");
    }

    @Bean public Jackson2JsonMessageConverter messageConverter() { return new Jackson2JsonMessageConverter(); }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf) {
        RabbitTemplate t = new RabbitTemplate(cf);
        t.setMessageConverter(messageConverter());
        return t;
    }
}
