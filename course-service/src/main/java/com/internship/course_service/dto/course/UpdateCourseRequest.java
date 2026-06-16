package com.internship.course_service.dto.course;

import com.internship.course_service.enums.CourseStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCourseRequest {

    private String title;

    private String description;

    private String category;

    private String difficulty;

    private List<String> prerequisites;

    private List<String> skillsYouWillLearn;

    @Valid
    private List<CourseModuleRequest> modules;

    private Integer durationInWeeks;

    @Min(value = 1, message = "Seats must be at least 1")
    private Integer availableSeats;

    private CourseStatus status;
}