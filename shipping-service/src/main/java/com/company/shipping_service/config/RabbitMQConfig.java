package com.company.shipping_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String SHIPPING_EXCHANGE = "shipping.events";
    public static final String ORDER_EXCHANGE    = "order.events";

    public static final String SHIPMENT_DISPATCHED_KEY = "shipment.dispatched";
    public static final String SHIPMENT_DELIVERED_KEY  = "shipment.delivered";

    public static final String SHIPPING_ORDER_CONFIRMED_QUEUE = "shipping.order.confirmed.q";

    @Bean public TopicExchange shippingExchange() { return new TopicExchange(SHIPPING_EXCHANGE, true, false); }
    @Bean public TopicExchange orderExchange()    { return new TopicExchange(ORDER_EXCHANGE, true, false); }

    @Bean public Queue shippingOrderConfirmedQueue() { return QueueBuilder.durable(SHIPPING_ORDER_CONFIRMED_QUEUE).build(); }

    @Bean
    public Binding shippingOrderBinding() {
        return BindingBuilder.bind(shippingOrderConfirmedQueue()).to(orderExchange()).with("order.confirmed");
    }

    @Bean public Jackson2JsonMessageConverter messageConverter() { return new Jackson2JsonMessageConverter(); }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf) {
        RabbitTemplate t = new RabbitTemplate(cf);
        t.setMessageConverter(messageConverter());
        return t;
    }
}
