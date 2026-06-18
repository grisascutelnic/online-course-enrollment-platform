package com.internship.course_service.ai.dto;

import com.internship.course_service.enums.CourseStatus;

public record TeacherCourseStatsAiResponse(
        String courseTitle,
        String category,
        String difficulty,
        Integer durationInWeeks,
        Integer availableSeats,
        CourseStatus status,
        Long totalEnrollments
) {
}