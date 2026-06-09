package com.internship.course_service.controller;

import com.internship.course_service.dto.course.CreateCourseRequest;
import com.internship.course_service.dto.course.UpdateCourseRequest;
import com.internship.course_service.entity.Course;
import com.internship.course_service.enums.CourseStatus;
import com.internship.course_service.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public Course createCourse(
            @Valid @RequestBody CreateCourseRequest request,
            Authentication authentication
    ) {
        return courseService.createCourse(
                request,
                authentication.getName()
        );
    }

    @GetMapping
    public List<Course> getAllCourses() {
        return courseService.getAllCourses();
    }

    @GetMapping("/{id}")
    public Course getCourseById(@PathVariable String id) {
        return courseService.getCourseById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public Course updateCourse(@PathVariable String id,
                               @RequestBody UpdateCourseRequest request) {
        return courseService.updateCourse(id, request);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('TEACHER')")
    public Course updateStatus(@PathVariable String id,
                               @RequestParam CourseStatus status) {
        return courseService.updateStatus(id, status);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public void deleteCourse(@PathVariable String id) {
        courseService.deleteCourse(id);
    }

    @PostMapping("/{id}/enrollment-requests")
    @PreAuthorize("hasRole('STUDENT')")
    public void requestEnrollment(
            @PathVariable String id,
            Authentication authentication
    ) {
        courseService.requestEnrollment(
                id,
                authentication.getName()
        );
    }
}