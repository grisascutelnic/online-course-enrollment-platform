package com.internship.enrollment_service.repository;

import com.internship.enrollment_service.entity.Enrollment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends MongoRepository<Enrollment, String> {

    List<Enrollment> findByStudentUsername(String studentUsername);

    List<Enrollment> findByCourseId(String courseId);

    Optional<Enrollment> findByStudentUsernameAndCourseId(String studentUsername, String courseId);

    boolean existsByStudentUsernameAndCourseId(String studentUsername, String courseId);
}