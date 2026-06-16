package com.internship.course_service.entity;

import com.internship.course_service.enums.CourseStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "courses")
public class Course {

    @Id
    private String id;

    private String title;

    private String description;

    private String category;

    private String difficulty;

    private List<String> prerequisites;

    private List<String> skillsYouWillLearn;

    private List<CourseModule> modules;

    private Integer durationInWeeks;

    private Integer availableSeats;

    private CourseStatus status;

    private String teacherUsername;
}