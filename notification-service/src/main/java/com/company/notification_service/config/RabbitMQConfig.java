package com.company.notification_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    /* Exchanges (re-declare; safe) */
    public static final String AUTH_EXCHANGE     = "auth.events";
    public static final String ORDER_EXCHANGE    = "order.events";
    public static final String PAYMENT_EXCHANGE  = "payment.events";
    public static final String SHIPPING_EXCHANGE = "shipping.events";
    public static final String INVENTORY_EXCHANGE = "inventory.events";

    /* Queues owned by notification-service */
    public static final String NOTIF_AUTH_QUEUE     = "notification.auth.q";
    public static final String NOTIF_ORDER_QUEUE    = "notification.order.q";
    public static final String NOTIF_PAYMENT_QUEUE  = "notification.payment.q";
    public static final String NOTIF_SHIPPING_QUEUE = "notification.shipping.q";
    public static final String NOTIF_INVENTORY_QUEUE = "notification.inventory.q";

    @Bean public TopicExchange authExchange()     { return new TopicExchange(AUTH_EXCHANGE, true, false); }
    @Bean public TopicExchange orderExchange()    { return new TopicExchange(ORDER_EXCHANGE, true, false); }
    @Bean public TopicExchange paymentExchange()  { return new TopicExchange(PAYMENT_EXCHANGE, true, false); }
    @Bean public TopicExchange shippingExchange() { return new TopicExchange(SHIPPING_EXCHANGE, true, false); }
    @Bean public TopicExchange inventoryExchange(){ return new TopicExchange(INVENTORY_EXCHANGE, true, false); }

    @Bean public Queue notifAuthQueue()     { return QueueBuilder.durable(NOTIF_AUTH_QUEUE).build(); }
    @Bean public Queue notifOrderQueue()    { return QueueBuilder.durable(NOTIF_ORDER_QUEUE).build(); }
    @Bean public Queue notifPaymentQueue()  { return QueueBuilder.durable(NOTIF_PAYMENT_QUEUE).build(); }
    @Bean public Queue notifShippingQueue() { return QueueBuilder.durable(NOTIF_SHIPPING_QUEUE).build(); }
    @Bean public Queue notifInventoryQueue(){ return QueueBuilder.durable(NOTIF_INVENTORY_QUEUE).build(); }

    @Bean public Binding authBinding()     { return BindingBuilder.bind(notifAuthQueue()).to(authExchange()).with("user.security.#"); }
    @Bean public Binding orderBinding()    { return BindingBuilder.bind(notifOrderQueue()).to(orderExchange()).with("order.#"); }
    @Bean public Binding paymentBinding()  { return BindingBuilder.bind(notifPaymentQueue()).to(paymentExchange()).with("payment.#"); }
    @Bean public Binding shippingBinding() { return BindingBuilder.bind(notifShippingQueue()).to(shippingExchange()).with("shipment.#"); }
    @Bean public Binding inventoryBinding(){ return BindingBuilder.bind(notifInventoryQueue()).to(inventoryExchange()).with("inventory.#"); }

    @Bean public Jackson2JsonMessageConverter messageConverter() { return new Jackson2JsonMessageConverter(); }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf) {
        RabbitTemplate t = new RabbitTemplate(cf);
        t.setMessageConverter(messageConverter());
        return t;
    }
}
