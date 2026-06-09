package com.internship.enrollment_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String ENROLLMENT_EXCHANGE = "enrollment.exchange";
    public static final String ENROLLMENT_REQUESTED_QUEUE = "enrollment.requested.queue";
    public static final String ENROLLMENT_REQUESTED_ROUTING_KEY = "enrollment.requested";

    @Bean
    public DirectExchange enrollmentExchange() {
        return new DirectExchange(ENROLLMENT_EXCHANGE);
    }

    @Bean
    public Queue enrollmentRequestedQueue() {
        return QueueBuilder
                .durable(ENROLLMENT_REQUESTED_QUEUE)
                .build();
    }

    @Bean
    public Binding enrollmentRequestedBinding() {
        return BindingBuilder
                .bind(enrollmentRequestedQueue())
                .to(enrollmentExchange())
                .with(ENROLLMENT_REQUESTED_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}