package com.internship.course_service.dto;

import com.internship.course_service.enums.CourseStatus;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCourseRequest {

    private String title;
    private String description;

    @Min(value = 1, message = "Seats must be at least 1")
    private Integer availableSeats;

    private CourseStatus status;
}