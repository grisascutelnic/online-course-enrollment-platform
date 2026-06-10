package com.internship.enrollment_service.client;

import com.internship.enrollment_service.dto.course.CourseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "course-service",
        url = "${services.course-service.url}"
)
public interface CourseClient {

    @GetMapping("/courses/{id}")
    CourseResponse getCourseById(@PathVariable String id);
}