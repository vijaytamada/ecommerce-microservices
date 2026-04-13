package com.company.order_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    /* ---- Exchanges ---- */
    public static final String ORDER_EXCHANGE   = "order.events";
    public static final String PAYMENT_EXCHANGE = "payment.events";
    public static final String SHIPPING_EXCHANGE = "shipping.events";

    /* ---- Routing Keys published ---- */
    public static final String ORDER_CREATED_KEY   = "order.created";
    public static final String ORDER_CONFIRMED_KEY = "order.confirmed";
    public static final String ORDER_CANCELLED_KEY = "order.cancelled";
    public static final String ORDER_SHIPPED_KEY   = "order.shipped";
    public static final String ORDER_DELIVERED_KEY = "order.delivered";

    /* ---- Queues owned by order-service ---- */
    public static final String ORDER_PAYMENT_QUEUE  = "order.payment.events.q";
    public static final String ORDER_SHIPPING_QUEUE = "order.shipping.events.q";

    @Bean public TopicExchange orderExchange()    { return new TopicExchange(ORDER_EXCHANGE, true, false); }
    @Bean public TopicExchange paymentExchange()  { return new TopicExchange(PAYMENT_EXCHANGE, true, false); }
    @Bean public TopicExchange shippingExchange() { return new TopicExchange(SHIPPING_EXCHANGE, true, false); }

    @Bean public Queue orderPaymentQueue()  { return QueueBuilder.durable(ORDER_PAYMENT_QUEUE).build(); }
    @Bean public Queue orderShippingQueue() { return QueueBuilder.durable(ORDER_SHIPPING_QUEUE).build(); }

    @Bean
    public Binding paymentBinding() {
        return BindingBuilder.bind(orderPaymentQueue()).to(paymentExchange()).with("payment.#");
    }

    @Bean
    public Binding shippingBinding() {
        return BindingBuilder.bind(orderShippingQueue()).to(shippingExchange()).with("shipment.#");
    }

    @Bean public Jackson2JsonMessageConverter messageConverter() { return new Jackson2JsonMessageConverter(); }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf) {
        RabbitTemplate t = new RabbitTemplate(cf);
        t.setMessageConverter(messageConverter());
        return t;
    }
}
