package com.internship.course_service.dto.enrollment;

import com.internship.course_service.enums.EnrollmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentResponse {

    private String id;

    private String courseId;

    private String studentUsername;

    private EnrollmentStatus status;

    private String teacherUsername;

    private LocalDateTime createdAt;
}