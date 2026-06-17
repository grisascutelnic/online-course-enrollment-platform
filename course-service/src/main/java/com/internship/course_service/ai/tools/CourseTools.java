package com.internship.course_service.ai.tools;

import com.internship.course_service.entity.Course;
import com.internship.course_service.entity.CourseModule;
import com.internship.course_service.enums.CourseStatus;
import com.internship.course_service.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CourseTools {

    private final CourseService courseService;

    @Tool(description = """
            Search courses by keyword across the whole course content.
            Searches in title, description, category, difficulty, prerequisites,
            skills, module titles, module content, module summaries, and module topics.
            Returns only courses that are open for enrollment.
            """)
    public List<Course> searchCourses(String keyword) {
        String normalizedKeyword = normalize(keyword);

        return courseService.getAllCourses()
                .stream()
                .filter(course -> course.getStatus() == CourseStatus.OPEN)
                .filter(course -> matchesCourse(course, normalizedKeyword))
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