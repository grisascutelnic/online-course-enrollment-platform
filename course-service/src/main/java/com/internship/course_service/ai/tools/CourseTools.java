package com.internship.course_service.ai.tools;

import com.internship.course_service.ai.dto.CourseSearchResult;
import com.internship.course_service.ai.dto.EnrollmentAiResponse;
import com.internship.course_service.client.EnrollmentClient;
import com.internship.course_service.entity.Course;
import com.internship.course_service.entity.CourseModule;
import com.internship.course_service.enums.CourseStatus;
import com.internship.course_service.exception.CourseNotFoundException;
import com.internship.course_service.service.CourseService;
import com.internship.enrollment_service.dto.enrollment.EnrollmentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CourseTools {

    private final CourseService courseService;
    private final EnrollmentClient enrollmentClient;

    @Tool(description = """
            Search courses by keyword across the whole course content.
            Searches in title, description, category, difficulty, prerequisites,
            skills, module titles, module content, module summaries, and module topics.
            Returns only courses that are open for enrollment.
            """)
    public List<CourseSearchResult> searchCourses(String keyword) {
        String normalizedKeyword = normalize(keyword);

        return courseService.getAllCourses()
                .stream()
                .filter(course -> course.getStatus() == CourseStatus.OPEN)
                .filter(course -> matchesCourse(course, normalizedKeyword))
                .map(course -> new CourseSearchResult(
                        course.getId(),
                        course.getTitle(),
                        course.getCategory(),
                        course.getDifficulty(),
                        course.getDurationInWeeks(),
                        course.getAvailableSeats(),
                        course.getStatus()
                ))
                .toList();
    }

    @Tool(description = """
        Explain a specific course in detail.
        Use this tool when the user asks for more information about one course,
        asks what a course teaches, asks about prerequisites, skills, modules,
        duration, difficulty, or whether a course contains a specific topic.
        The input can be a course title, partial title, or course id.
        """)
    public Course explainCourse(String courseIdentifier) {
        String normalizedIdentifier = normalize(courseIdentifier);

        return courseService.getAllCourses()
                .stream()
                .filter(course ->
                        contains(course.getId(), normalizedIdentifier)
                                || contains(course.getTitle(), normalizedIdentifier)
                )
                .findFirst()
                .orElseThrow(() -> new CourseNotFoundException("Course not found"));
    }

    @Tool(description = """
        Compare two or more courses.

        Use this tool when the user asks to compare courses,
        compare learning paths, compare difficulty, skills,
        prerequisites, duration, modules, or available seats.

        The user may provide full course titles or partial titles.
        Return the matching courses for comparison.
        """)
    public List<Course> compareCourses(String courseIdentifiers) {
        List<String> identifiers = List.of(courseIdentifiers.split(","))
                .stream()
                .map(this::normalize)
                .filter(identifier -> !identifier.isBlank())
                .toList();

        return courseService.getAllCourses()
                .stream()
                .filter(course -> identifiers.stream()
                        .anyMatch(identifier ->
                                contains(course.getId(), identifier)
                                        || contains(course.getTitle(), identifier)
                        )
                )
                .toList();
    }

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

    private boolean matchesCourse(Course course, String keyword) {
        if (keyword.isBlank()) {
            return true;
        }

        return contains(course.getTitle(), keyword)
                || contains(course.getDescription(), keyword)
                || contains(course.getCategory(), keyword)
                || contains(course.getDifficulty(), keyword)
                || contains(course.getPrerequisites(), keyword)
                || contains(course.getSkillsYouWillLearn(), keyword)
                || containsModules(course.getModules(), keyword);
    }

    private boolean containsModules(List<CourseModule> modules, String keyword) {
        if (modules == null) {
            return false;
        }

        return modules.stream()
                .anyMatch(module ->
                        contains(module.getTitle(), keyword)
                                || contains(module.getContent(), keyword)
                                || contains(module.getSummary(), keyword)
                                || contains(module.getTopics(), keyword)
                );
    }

    private boolean contains(String value, String keyword) {
        return value != null && normalize(value).contains(keyword);
    }

    private boolean contains(List<String> values, String keyword) {
        return values != null && values.stream()
                .anyMatch(value -> contains(value, keyword));
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase().trim();
    }
}