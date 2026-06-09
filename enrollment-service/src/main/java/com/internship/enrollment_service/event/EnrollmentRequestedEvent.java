package com.internship.enrollment_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentRequestedEvent {

    private String eventId;

    private String courseId;

    private String studentUsername;

    private LocalDateTime requestedAt;
}