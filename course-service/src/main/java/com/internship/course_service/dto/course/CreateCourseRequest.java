package com.internship.course_service.dto.course;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCourseRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Category is required")
    private String category;

    @NotBlank(message = "Difficulty is required")
    private String difficulty;

    private List<String> prerequisites;

    @NotEmpty(message = "Skills are required")
    private List<String> skillsYouWillLearn;

    @Valid
    private List<CourseModuleRequest> modules;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 week")
    private Integer durationInWeeks;

    @NotNull(message = "Available seats is required")
    @Min(value = 1, message = "Seats must be at least 1")
    private Integer availableSeats;
}