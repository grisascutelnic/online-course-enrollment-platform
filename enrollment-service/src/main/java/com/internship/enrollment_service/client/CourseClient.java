package com.internship.enrollment_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "course-service")
public interface CourseClient {

    @PatchMapping("/courses/{id}/restore-seat")
    void restoreSeat(@PathVariable("id") String id);
}