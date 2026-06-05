package com.internship.course_service.publisher;

import com.internship.course_service.config.RabbitMQConfig;
import com.internship.course_service.event.EnrollmentRequestedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EnrollmentEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishEnrollmentRequested(
            EnrollmentRequestedEvent event
    ) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ENROLLMENT_EXCHANGE,
                RabbitMQConfig.ENROLLMENT_REQUESTED_ROUTING_KEY,
                event
        );
    }
}