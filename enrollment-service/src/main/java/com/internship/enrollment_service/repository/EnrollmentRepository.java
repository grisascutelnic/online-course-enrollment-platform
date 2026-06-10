package com.internship.enrollment_service.repository;

import com.internship.enrollment_service.entity.Enrollment;
import com.internship.enrollment_service.enums.EnrollmentStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends MongoRepository<Enrollment, String> {

    List<Enrollment> findByStudentUsername(String studentUsername);

    boolean existsByStudentUsernameAndCourseId(String studentUsername, String courseId);

    List<Enrollment> findByTeacherUsername(String teacherUsername);

    Long countByCourseId(String courseId);
}