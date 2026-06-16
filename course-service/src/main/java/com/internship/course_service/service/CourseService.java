package com.internship.course_service.service;

import com.internship.course_service.client.EnrollmentClient;
import com.internship.course_service.dto.course.CourseModuleRequest;
import com.internship.course_service.dto.course.CourseStatsResponse;
import com.internship.course_service.dto.course.CreateCourseRequest;
import com.internship.course_service.dto.course.UpdateCourseRequest;
import com.internship.course_service.dto.enrollment.EnrollmentStatsResponse;
import com.internship.course_service.entity.Course;
import com.internship.course_service.entity.CourseModule;
import com.internship.course_service.enums.CourseStatus;
import com.internship.course_service.event.EnrollmentRequestedEvent;
import com.internship.course_service.exception.CourseEnrollmentException;
import com.internship.course_service.exception.CourseNotFoundException;
import com.internship.course_service.publisher.EnrollmentEventPublisher;
import com.internship.course_service.repository.CourseRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final EnrollmentEventPublisher enrollmentEventPublisher;
    private final EnrollmentClient enrollmentClient;

    public CourseService(CourseRepository courseRepository,
                         EnrollmentEventPublisher enrollmentEventPublisher,
                         EnrollmentClient enrollmentClient) {
        this.courseRepository = courseRepository;
        this.enrollmentEventPublisher = enrollmentEventPublisher;
        this.enrollmentClient = enrollmentClient;
    }

    public Course createCourse(
            CreateCourseRequest request,
            String teacherUsername
    ) {

        Course course = new Course();

        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setCategory(request.getCategory());
        course.setDifficulty(request.getDifficulty());
        course.setPrerequisites(request.getPrerequisites());
        course.setSkillsYouWillLearn(request.getSkillsYouWillLearn());
        course.setModules(mapModules(request.getModules()));
        course.setDurationInWeeks(request.getDurationInWeeks());
        course.setAvailableSeats(request.getAvailableSeats());
        course.setStatus(CourseStatus.OPEN);
        course.setTeacherUsername(teacherUsername);

        return courseRepository.save(course);
    }

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public Course getCourseById(String id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new CourseNotFoundException("Course not found"));
    }

    public Course updateCourse(String id, UpdateCourseRequest request, Authentication authentication) {

        Course course = getCourseById(id);

        validateCourseAccess(course, authentication);

        if (request.getTitle() != null) {
            course.setTitle(request.getTitle());
        }

        if (request.getDescription() != null) {
            course.setDescription(request.getDescription());
        }

        if (request.getCategory() != null) {
            course.setCategory(request.getCategory());
        }

        if (request.getDifficulty() != null) {
            course.setDifficulty(request.getDifficulty());
        }

        if (request.getPrerequisites() != null) {
            course.setPrerequisites(request.getPrerequisites());
        }

        if (request.getSkillsYouWillLearn() != null) {
            course.setSkillsYouWillLearn(request.getSkillsYouWillLearn());
        }

        if (request.getModules() != null) {
            course.setModules(mapModules(request.getModules()));
        }

        if (request.getDurationInWeeks() != null) {
            course.setDurationInWeeks(request.getDurationInWeeks());
        }

        if (request.getAvailableSeats() != null) {
            course.setAvailableSeats(request.getAvailableSeats());

            if (request.getAvailableSeats() > 0) {
                course.setStatus(CourseStatus.OPEN);
            } else {
                course.setStatus(CourseStatus.CLOSED);
            }
        }

        return courseRepository.save(course);
    }

    public Course updateStatus(String id, CourseStatus status, Authentication authentication) {

        Course course = getCourseById(id);

        validateCourseAccess(course, authentication);

        course.setStatus(status);

        return courseRepository.save(course);
    }

    public void deleteCourse(String id, Authentication authentication) {
        Course course = getCourseById(id);

        validateCourseAccess(course, authentication);

        courseRepository.deleteById(id);
    }

    private void validateCourseAccess(Course course, Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            return;
        }

        String currentUsername = authentication.getName();

        if (!course.getTeacherUsername().equals(currentUsername)) {
            throw new CourseEnrollmentException("You are not allowed to access this course");
        }
    }

    public void requestEnrollment(
            String courseId,
            String studentUsername
    ) {
        Course course = getCourseById(courseId);

        if (course.getStatus() != CourseStatus.OPEN) {
            throw new CourseEnrollmentException("Course is not open for enrollment");
        }

        boolean alreadyRequested = enrollmentClient.existsEnrollment(
                courseId,
                studentUsername
        );

        if (alreadyRequested) {
            throw new CourseEnrollmentException(
                    "Student already requested enrollment for this course"
            );
        }

        if (course.getAvailableSeats() == null || course.getAvailableSeats() <= 0) {
            throw new CourseEnrollmentException("No available seats for this course");
        }

        course.setAvailableSeats(course.getAvailableSeats() - 1);

        if (course.getAvailableSeats() == 0) {
            course.setStatus(CourseStatus.CLOSED);
        }

        courseRepository.save(course);

        EnrollmentRequestedEvent event = EnrollmentRequestedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .courseId(course.getId())
                .studentUsername(studentUsername)
                .teacherUsername(course.getTeacherUsername())
                .requestedAt(LocalDateTime.now())
                .build();

        enrollmentEventPublisher.publishEnrollmentRequested(event);
    }

    public CourseStatsResponse getCourseStats(String courseId, Authentication authentication) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("Course not found"));

        validateCourseAccess(course, authentication);

        EnrollmentStatsResponse enrollmentStats =
                enrollmentClient.getStatsByCourseId(courseId);

        return new CourseStatsResponse(
                course.getId(),
                course.getTitle(),
                enrollmentStats.getTotalEnrollments()
        );
    }

    private List<CourseModule> mapModules(List<CourseModuleRequest> moduleRequests) {
        if (moduleRequests == null) {
            return null;
        }

        return moduleRequests.stream()
                .map(moduleRequest -> new CourseModule(
                        moduleRequest.getOrder(),
                        moduleRequest.getTitle(),
                        moduleRequest.getContent(),
                        moduleRequest.getSummary(),
                        moduleRequest.getTopics(),
                        moduleRequest.getEstimatedHours()
                ))
                .toList();
    }
}