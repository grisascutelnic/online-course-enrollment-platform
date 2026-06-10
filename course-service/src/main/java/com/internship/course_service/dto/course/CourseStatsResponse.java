package com.internship.course_service.dto.course;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseStatsResponse {

    private String courseId;
    private String title;
    private Long totalEnrollments;
}