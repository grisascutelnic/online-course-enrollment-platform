package com.internship.course_service.client;


import com.internship.course_service.config.FeignAuthConfig;
import com.internship.course_service.dto.enrollment.EnrollmentStatsResponse;
import com.internship.course_service.dto.enrollment.EnrollmentResponse;
import com.internship.course_service.dto.enrollment.UpdateEnrollmentStatusRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/enrollments/teacher/me")
    List<EnrollmentResponse> getMyTeacherEnrollments();

    @PatchMapping("/enrollments/{id}/status")
    EnrollmentResponse updateEnrollmentStatus(
            @PathVariable String id,
            @RequestBody UpdateEnrollmentStatusRequest request
    );
}