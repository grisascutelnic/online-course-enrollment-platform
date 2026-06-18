package com.internship.course_service.ai.dto;

import java.time.LocalDateTime;

public record TeacherEnrollmentAiResponse(
        String courseTitle,
        String studentUsername,
        String status,
        LocalDateTime requestedAt
) {
}