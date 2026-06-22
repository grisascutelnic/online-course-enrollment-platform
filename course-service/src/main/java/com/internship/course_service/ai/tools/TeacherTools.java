package com.internship.course_service.ai.tools;

import com.internship.course_service.ai.dto.TeacherCourseStatsAiResponse;
import com.internship.course_service.ai.dto.TeacherEnrollmentAiResponse;
import com.internship.course_service.client.EnrollmentClient;
import com.internship.course_service.dto.enrollment.EnrollmentResponse;
import com.internship.course_service.dto.enrollment.EnrollmentStatsResponse;
import com.internship.course_service.dto.enrollment.UpdateEnrollmentStatusRequest;
import com.internship.course_service.entity.Course;
import com.internship.course_service.entity.CourseModule;
import com.internship.course_service.enums.EnrollmentStatus;
import com.internship.course_service.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TeacherTools {

    private final CourseService courseService;
    private final EnrollmentClient enrollmentClient;

    @Tool(description = """
        Get enrollment requests for the logged-in teacher.

        Can filter by course title and/or enrollment status.
        If courseTitle is empty, return enrollments for all courses owned by the teacher.
        If status is empty, return enrollments with all statuses.
        Use this tool when the teacher asks to view enrollments, pending requests,
        approved requests, rejected requests, or enrollments for a specific course.
        Show data in tables.
        """)
    public List<TeacherEnrollmentAiResponse> getTeacherEnrollments(
            String courseTitle,
            String status
    ) {
        String normalizedCourseTitle = normalize(courseTitle);
        String normalizedStatus = normalize(status);

        return enrollmentClient.getMyTeacherEnrollments()
                .stream()
                .filter(enrollment -> {
                    Course course = courseService.getCourseById(enrollment.getCourseId());

                    boolean matchesCourse =
                            normalizedCourseTitle.isBlank()
                                    || contains(course.getTitle(), normalizedCourseTitle);

                    boolean matchesStatus =
                            normalizedStatus.isBlank()
                                    || normalize(enrollment.getStatus().toString())
                                    .equals(normalizedStatus);

                    return matchesCourse && matchesStatus;
                })
                .map(enrollment -> {
                    Course course = courseService.getCourseById(enrollment.getCourseId());

                    return new TeacherEnrollmentAiResponse(
                            course.getTitle(),
                            enrollment.getStudentUsername(),
                            enrollment.getStatus().toString(),
                            enrollment.getCreatedAt()
                    );
                })
                .toList();
    }

    @Tool(description = """
        Update an enrollment request status for the logged-in teacher.

        Use this tool when a teacher clearly asks to change a student's enrollment status.
        Allowed statuses are: PENDING, APPROVED, REJECTED, COMPLETED, CANCELLED.
        The input should include the course title, student username, and new status.
        """)
    public String updateEnrollmentStatus(
            String courseTitle,
            String studentUsername,
            String status
    ) {
        String normalizedCourseTitle = normalize(courseTitle);
        String normalizedStudentUsername = normalize(studentUsername);
        String normalizedStatus = normalize(status);

        EnrollmentStatus enrollmentStatus;

        try {
            enrollmentStatus = EnrollmentStatus.valueOf(
                    normalizedStatus.toUpperCase()
            );
        } catch (IllegalArgumentException exception) {
            return "Invalid status. Allowed statuses are: PENDING, APPROVED, REJECTED, COMPLETED, CANCELLED.";
        }

        EnrollmentResponse enrollment = enrollmentClient.getMyTeacherEnrollments()
                .stream()
                .filter(existingEnrollment -> {
                    Course course = courseService.getCourseById(
                            existingEnrollment.getCourseId()
                    );

                    return contains(course.getTitle(), normalizedCourseTitle)
                            && contains(existingEnrollment.getStudentUsername(), normalizedStudentUsername);
                })
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Enrollment request not found"));

        EnrollmentResponse updatedEnrollment =
                enrollmentClient.updateEnrollmentStatus(
                        enrollment.getId(),
                        new UpdateEnrollmentStatusRequest(enrollmentStatus)
                );

        Course course = courseService.getCourseById(updatedEnrollment.getCourseId());

        return "Enrollment status updated successfully. Course: "
                + course.getTitle()
                + ", student: "
                + updatedEnrollment.getStudentUsername()
                + ", status: "
                + updatedEnrollment.getStatus()
                + ".";
    }

    @Tool(description = """
        Get courses created by the logged-in teacher together with course statistics.

        Use this tool when the teacher asks to see their courses,
        course statistics, available seats, total enrollments,
        or performance of their own courses.

        Returns only courses owned by the logged-in teacher.
        """)
    public List<TeacherCourseStatsAiResponse> getMyCourseStats() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        String teacherUsername = authentication.getName();

        boolean isTeacher = authentication.getAuthorities()
                .stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_TEACHER"));

        if (!isTeacher) {
            throw new RuntimeException("Only teachers can view their course statistics.");
        }

        return courseService.getAllCourses()
                .stream()
                .filter(course -> teacherUsername.equals(course.getTeacherUsername()))
                .map(course -> {
                    EnrollmentStatsResponse stats =
                            enrollmentClient.getStatsByCourseId(course.getId());

                    return new TeacherCourseStatsAiResponse(
                            course.getTitle(),
                            course.getCategory(),
                            course.getDifficulty(),
                            course.getDurationInWeeks(),
                            course.getAvailableSeats(),
                            course.getStatus(),
                            stats.getTotalEnrollments()
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
