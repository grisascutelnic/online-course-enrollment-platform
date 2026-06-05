package com.internship.course_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String ENROLLMENT_EXCHANGE = "enrollment.exchange";
    public static final String ENROLLMENT_REQUESTED_QUEUE = "enrollment.requested.queue";
    public static final String ENROLLMENT_REQUESTED_ROUTING_KEY = "enrollment.requested";

    @Bean
    public AmqpAdmin amqpAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

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

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter messageConverter
    ) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }
}