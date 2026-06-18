package com.internship.course_service.ai.dto;

import java.time.LocalDateTime;

public record EnrollmentAiResponse(

        String courseTitle,

        String teacherUsername,

        String status,

        LocalDateTime requestedAt

) {
}