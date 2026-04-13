package com.company.inventory_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    /* ---- Exchanges ---- */
    public static final String ORDER_EXCHANGE     = "order.events";
    public static final String PAYMENT_EXCHANGE   = "payment.events";
    public static final String INVENTORY_EXCHANGE = "inventory.events";

    /* ---- Queues owned by inventory-service ---- */
    public static final String INV_ORDER_CANCELLED_QUEUE = "inventory.order.cancelled.q";
    public static final String INV_PAYMENT_EVENTS_QUEUE  = "inventory.payment.events.q";

    /* ---- Routing Keys consumed ---- */
    public static final String ORDER_CANCELLED_KEY = "order.cancelled";
    public static final String PAYMENT_ALL_KEY     = "payment.#";

    /* ---- Routing Keys published ---- */
    public static final String INVENTORY_LOW_STOCK_KEY = "inventory.low.stock";

    /* ---- Exchanges (declare; others declared by their owners but safe to re-declare) ---- */
    @Bean public TopicExchange orderExchange()     { return new TopicExchange(ORDER_EXCHANGE, true, false); }
    @Bean public TopicExchange paymentExchange()   { return new TopicExchange(PAYMENT_EXCHANGE, true, false); }
    @Bean public TopicExchange inventoryExchange() { return new TopicExchange(INVENTORY_EXCHANGE, true, false); }

    /* ---- Queues ---- */
    @Bean public Queue invOrderCancelledQueue() { return QueueBuilder.durable(INV_ORDER_CANCELLED_QUEUE).build(); }
    @Bean public Queue invPaymentEventsQueue()  { return QueueBuilder.durable(INV_PAYMENT_EVENTS_QUEUE).build(); }

    /* ---- Bindings ---- */
    @Bean
    public Binding invOrderCancelledBinding() {
        return BindingBuilder.bind(invOrderCancelledQueue()).to(orderExchange()).with(ORDER_CANCELLED_KEY);
    }

    @Bean
    public Binding invPaymentEventsBinding() {
        return BindingBuilder.bind(invPaymentEventsQueue()).to(paymentExchange()).with(PAYMENT_ALL_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() { return new Jackson2JsonMessageConverter(); }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf) {
        RabbitTemplate t = new RabbitTemplate(cf);
        t.setMessageConverter(messageConverter());
        return t;
    }
}
