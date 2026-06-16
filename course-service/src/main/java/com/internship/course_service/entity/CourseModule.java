package com.internship.course_service.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseModule {

    private Integer order;

    private String title;

    private String content;

    private String summary;

    private List<String> topics;

    private Integer estimatedHours;
}