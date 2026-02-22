package com.company.auth_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    /* Exchange */
    public static final String AUTH_EXCHANGE = "auth.events";

    /* Queues */
    public static final String USER_CREATED_QUEUE = "user.created.q";
    public static final String USER_UPDATED_QUEUE = "user.updated.q";
    public static final String USER_DISABLED_QUEUE = "user.disabled.q";
    public static final String USER_SECURITY_QUEUE = "user.security.q";

    /* Routing Keys */
    public static final String USER_CREATED_KEY = "user.created";
    public static final String USER_UPDATED_KEY = "user.updated";
    public static final String USER_DISABLED_KEY = "user.disabled";

    /* Security Events (Wildcard) */
    public static final String USER_SECURITY_KEY = "user.security.#";


    /* Exchange */
    @Bean
    public TopicExchange authExchange() {
        return new TopicExchange(AUTH_EXCHANGE, true, false);
    }


    /* Queues */

    @Bean
    public Queue userCreatedQueue() {
        return QueueBuilder.durable(USER_CREATED_QUEUE).build();
    }

    @Bean
    public Queue userUpdatedQueue() {
        return QueueBuilder.durable(USER_UPDATED_QUEUE).build();
    }

    @Bean
    public Queue userDisabledQueue() {
        return QueueBuilder.durable(USER_DISABLED_QUEUE).build();
    }

    @Bean
    public Queue userSecurityQueue() {
        return QueueBuilder.durable(USER_SECURITY_QUEUE).build();
    }


    /* Bindings */

    @Bean
    public Binding userCreatedBinding() {
        return BindingBuilder
                .bind(userCreatedQueue())
                .to(authExchange())
                .with(USER_CREATED_KEY);
    }

    @Bean
    public Binding userUpdatedBinding() {
        return BindingBuilder
                .bind(userUpdatedQueue())
                .to(authExchange())
                .with(USER_UPDATED_KEY);
    }

    @Bean
    public Binding userDisabledBinding() {
        return BindingBuilder
                .bind(userDisabledQueue())
                .to(authExchange())
                .with(USER_DISABLED_KEY);
    }

    @Bean
    public Binding userSecurityBinding() {
        return BindingBuilder
                .bind(userSecurityQueue())
                .to(authExchange())
                .with(USER_SECURITY_KEY);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {

        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);

        // Enable mandatory flag so returns work
        rabbitTemplate.setMandatory(true);

        // Callback when message is NOT routed
        rabbitTemplate.setReturnsCallback(returned -> {

            System.out.println("❌ Message NOT routed!");

            System.out.println("Exchange   : " + returned.getExchange());
            System.out.println("RoutingKey : " + returned.getRoutingKey());
            System.out.println("ReplyCode  : " + returned.getReplyCode());
            System.out.println("ReplyText  : " + returned.getReplyText());
            System.out.println("Body       : " + new String(returned.getMessage().getBody()));
        });

        return rabbitTemplate;
    }
}
