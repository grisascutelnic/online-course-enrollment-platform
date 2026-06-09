package com.internship.enrollment_service.listener;

import com.internship.enrollment_service.config.RabbitMQConfig;
import com.internship.enrollment_service.event.EnrollmentRequestedEvent;
import com.internship.enrollment_service.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EnrollmentEventListener {

    private final EnrollmentService enrollmentService;

    @RabbitListener(queues = RabbitMQConfig.ENROLLMENT_REQUESTED_QUEUE)
    public void handleEnrollmentRequested(EnrollmentRequestedEvent event) {
        enrollmentService.createEnrollmentFromEvent(event);
    }
}