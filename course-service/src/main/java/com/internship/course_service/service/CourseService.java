package com.internship.course_service.service;

import com.internship.course_service.dto.CreateCourseRequest;
import com.internship.course_service.dto.UpdateCourseRequest;
import com.internship.course_service.entity.Course;
import com.internship.course_service.enums.CourseStatus;
import com.internship.course_service.repository.CourseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseService {

    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public Course createCourse(CreateCourseRequest request) {

        Course course = new Course();
        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setAvailableSeats(request.getAvailableSeats());
        course.setStatus(CourseStatus.OPEN);

        return courseRepository.save(course);
    }

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public Course getCourseById(String id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
    }

    public Course updateCourse(String id, UpdateCourseRequest request) {

        Course course = getCourseById(id);

        if (request.getTitle() != null) {
            course.setTitle(request.getTitle());
        }

        if (request.getDescription() != null) {
            course.setDescription(request.getDescription());
        }

        if (request.getAvailableSeats() != null) {
            course.setAvailableSeats(request.getAvailableSeats());
        }

        if (request.getStatus() != null) {
            course.setStatus(request.getStatus());
        }

        return courseRepository.save(course);
    }

    public Course updateStatus(String id, CourseStatus status) {

        Course course = getCourseById(id);

        course.setStatus(status);

        return courseRepository.save(course);
    }

    public void deleteCourse(String id) {
        courseRepository.deleteById(id);
    }
}