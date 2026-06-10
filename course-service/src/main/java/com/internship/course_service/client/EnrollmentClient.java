package com.internship.course_service.client;


import com.internship.course_service.dto.enrollment.EnrollmentStatsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "enrollment-service")
public interface EnrollmentClient {

    @GetMapping("/enrollments/course/{courseId}/stats")
    EnrollmentStatsResponse getStatsByCourseId(@PathVariable String courseId);
}