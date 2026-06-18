package com.internship.course_service.ai.dto;

import com.internship.course_service.enums.CourseStatus;

public record CourseSearchResult(
        String id,
        String title,
        String category,
        String difficulty,
        Integer durationInWeeks,
        Integer availableSeats,
        CourseStatus status
) {
}