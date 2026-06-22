package com.internship.course_service.ai.tools;

import com.internship.course_service.ai.dto.EnrollmentAiResponse;
import com.internship.course_service.client.EnrollmentClient;
import com.internship.course_service.dto.enrollment.EnrollmentResponse;
import com.internship.course_service.entity.Course;
import com.internship.course_service.exception.CourseNotFoundException;
import com.internship.course_service.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StudentTools {
    private final CourseService courseService;
    private final EnrollmentClient enrollmentClient;

    @Tool(description = """
        Request enrollment in a specific course.

        Use this tool only after the logged-in student clearly confirms
        that they want to enroll in the course.

        The input can be a course title, partial title, or course id.
        The student username is taken automatically from the logged-in user.
        Do not ask the student for their username.
        """)
    public String requestEnrollment(String courseIdentifier) {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        boolean isStudent = authentication.getAuthorities()
                .stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_STUDENT"));

        if (!isStudent) {
            return "Only students can request enrollment in courses.";
        }

        String studentUsername = authentication.getName();
        String normalizedIdentifier = normalize(courseIdentifier);

        Course course = courseService.getAllCourses()
                .stream()
                .filter(existingCourse ->
                        contains(existingCourse.getId(), normalizedIdentifier)
                                || contains(existingCourse.getTitle(), normalizedIdentifier)
                )
                .findFirst()
                .orElseThrow(() -> new CourseNotFoundException("Course not found"));

        courseService.requestEnrollment(course.getId(), studentUsername);

        return "Enrollment request submitted successfully for course: "
                + course.getTitle()
                + ". Status: PENDING.";
    }

    @Tool(description = """
        Delete the logged-in student's enrollment request for a specific course.

        Use this tool only when the student clearly asks to cancel, delete,
        remove, or withdraw their enrollment request.

        The input can be a course title, partial title, or course id.
        The student is identified automatically from the JWT token.
        """)
    public String deleteMyEnrollment(String courseIdentifier) {
        String normalizedIdentifier = normalize(courseIdentifier);

        EnrollmentResponse enrollment = enrollmentClient.getMyStudentEnrollments()
                .stream()
                .filter(existingEnrollment -> {
                    Course course = courseService.getCourseById(
                            existingEnrollment.getCourseId()
                    );

                    return contains(course.getId(), normalizedIdentifier)
                            || contains(course.getTitle(), normalizedIdentifier);
                })
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Enrollment request not found"));

        Course course = courseService.getCourseById(enrollment.getCourseId());

        enrollmentClient.deleteMyEnrollment(enrollment.getId());

        return "Enrollment request deleted successfully for course: "
                + course.getTitle()
                + ".";
    }

    @Tool(description = """
        Get enrollment requests for the logged-in student.
        Use this tool when the student asks to see their enrollments,
        enrollment requests, or enrollment statuses.
        Do not show course ids unless the user specifically asks for ids.
        The student is identified automatically from the JWT token.
        """)
    public List<EnrollmentAiResponse> getMyEnrollments() {
        return enrollmentClient.getMyStudentEnrollments()
                .stream()
                .map(enrollment -> {

                    Course course =
                            courseService.getCourseById(
                                    enrollment.getCourseId()
                            );

                    return new EnrollmentAiResponse(

                            course.getTitle(),

                            enrollment.getTeacherUsername(),

                            enrollment.getStatus().toString(),

                            enrollment.getCreatedAt()
                    );
                })
                .toList();
    }


    private boolean contains(String value, String keyword) {
        return value != null && normalize(value).contains(keyword);
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase().trim();
    }
}
