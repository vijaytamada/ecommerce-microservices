package com.company.user_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    /* ---- Exchanges (already declared by Auth Service, but safe to re-declare) ---- */
    public static final String AUTH_EXCHANGE = "auth.events";

    /* ---- Queues (user-service owns these) ---- */
    public static final String USER_PROFILE_CREATED_QUEUE  = "user.profile.created.q";
    public static final String USER_PROFILE_DISABLED_QUEUE = "user.profile.disabled.q";

    /* ---- Routing Keys (from auth-service) ---- */
    public static final String USER_CREATED_KEY  = "user.created";
    public static final String USER_DISABLED_KEY = "user.disabled";

    @Bean
    public TopicExchange authExchange() {
        return new TopicExchange(AUTH_EXCHANGE, true, false);
    }

    @Bean
    public Queue userProfileCreatedQueue() {
        return QueueBuilder.durable(USER_PROFILE_CREATED_QUEUE).build();
    }

    @Bean
    public Queue userProfileDisabledQueue() {
        return QueueBuilder.durable(USER_PROFILE_DISABLED_QUEUE).build();
    }

    @Bean
    public Binding userCreatedBinding() {
        return BindingBuilder
                .bind(userProfileCreatedQueue())
                .to(authExchange())
                .with(USER_CREATED_KEY);
    }

    @Bean
    public Binding userDisabledBinding() {
        return BindingBuilder
                .bind(userProfileDisabledQueue())
                .to(authExchange())
                .with(USER_DISABLED_KEY);
    }

    /* ---- JSON Converter ---- */
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
