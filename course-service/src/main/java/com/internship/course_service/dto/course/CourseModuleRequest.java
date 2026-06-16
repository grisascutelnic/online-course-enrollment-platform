package com.internship.course_service.dto.course;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseModuleRequest {

    @Min(value = 1, message = "Order must be at least 1")
    private Integer order;

    @NotBlank(message = "Module title is required")
    private String title;

    @NotBlank(message = "Content is required")
    private String content;

    @NotBlank(message = "Summary is required")
    private String summary;

    private List<String> topics;

    @Min(value = 1, message = "Estimated hours must be at least 1")
    private Integer estimatedHours;
}