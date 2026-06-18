package com.internship.course_service.client;


import com.internship.course_service.config.FeignAuthConfig;
import com.internship.course_service.dto.enrollment.EnrollmentStatsResponse;
import com.internship.enrollment_service.dto.enrollment.EnrollmentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(
        name = "enrollment-service",
        configuration = FeignAuthConfig.class
)
public interface EnrollmentClient {

    @GetMapping("/enrollments/course/{courseId}/stats")
    EnrollmentStatsResponse getStatsByCourseId(@PathVariable String courseId);

    @GetMapping("/enrollments/exists")
    boolean existsEnrollment(@RequestParam String courseId,
                             @RequestParam String studentUsername);

    @GetMapping("/enrollments/student/me")
    List<EnrollmentResponse> getMyStudentEnrollments();
}