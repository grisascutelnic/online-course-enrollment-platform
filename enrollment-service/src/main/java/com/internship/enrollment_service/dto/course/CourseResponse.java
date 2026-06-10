package com.internship.enrollment_service.dto.course;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseResponse {

    private String id;
    private String title;
    private String description;
    private Integer availableSeats;
    private String status;
}