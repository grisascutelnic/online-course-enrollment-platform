package com.internship.course_service.service;

import com.internship.course_service.dto.course.CreateCourseRequest;
import com.internship.course_service.dto.course.UpdateCourseRequest;
import com.internship.course_service.entity.Course;
import com.internship.course_service.enums.CourseStatus;
import com.internship.course_service.event.EnrollmentRequestedEvent;
import com.internship.course_service.exception.CourseEnrollmentException;
import com.internship.course_service.exception.CourseNotFoundException;
import com.internship.course_service.publisher.EnrollmentEventPublisher;
import com.internship.course_service.repository.CourseRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final EnrollmentEventPublisher enrollmentEventPublisher;

    public CourseService(CourseRepository courseRepository,
                         EnrollmentEventPublisher enrollmentEventPublisher) {
        this.courseRepository = courseRepository;
        this.enrollmentEventPublisher = enrollmentEventPublisher;
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
                .orElseThrow(() -> new CourseNotFoundException("Course not found"));
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

    public void requestEnrollment(
            String courseId,
            String studentUsername
    ) {
        Course course = getCourseById(courseId);

        if (course.getStatus() != CourseStatus.OPEN) {
            throw new CourseEnrollmentException("Course is not open for enrollment");
        }

        if (course.getAvailableSeats() == null || course.getAvailableSeats() <= 0) {
            throw new CourseEnrollmentException("No available seats for this course");
        }

        EnrollmentRequestedEvent event = EnrollmentRequestedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .courseId(course.getId())
                .studentUsername(studentUsername)
                .requestedAt(LocalDateTime.now())
                .build();

        enrollmentEventPublisher.publishEnrollmentRequested(event);
    }
}