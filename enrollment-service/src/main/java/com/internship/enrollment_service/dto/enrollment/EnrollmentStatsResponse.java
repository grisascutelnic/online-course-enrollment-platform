package com.internship.enrollment_service.dto.enrollment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EnrollmentStatsResponse {

    private String courseId;
    private Long totalEnrollments;
}