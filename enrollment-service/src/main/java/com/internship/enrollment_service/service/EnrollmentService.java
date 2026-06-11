package com.internship.enrollment_service.service;

import com.internship.enrollment_service.dto.enrollment.EnrollmentResponse;
import com.internship.enrollment_service.dto.enrollment.EnrollmentStatsResponse;
import com.internship.enrollment_service.dto.enrollment.UpdateEnrollmentStatusRequest;
import com.internship.enrollment_service.entity.Enrollment;
import com.internship.enrollment_service.enums.EnrollmentStatus;
import com.internship.enrollment_service.event.EnrollmentRequestedEvent;
import com.internship.enrollment_service.exception.EnrollmentNotFoundException;
import com.internship.enrollment_service.exception.InvalidEnrollmentStatusException;
import com.internship.enrollment_service.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;

    public void createEnrollmentFromEvent(EnrollmentRequestedEvent event) {

        boolean alreadyExists = enrollmentRepository
                .existsByStudentUsernameAndCourseId(
                        event.getStudentUsername(),
                        event.getCourseId()
                );

        if (alreadyExists) {
            return;
        }

        Enrollment enrollment = Enrollment.builder()
                .courseId(event.getCourseId())
                .studentUsername(event.getStudentUsername())
                .status(EnrollmentStatus.PENDING)
                .teacherUsername(event.getTeacherUsername())
                .createdAt(LocalDateTime.now())
                .build();

        enrollmentRepository.save(enrollment);
    }

    public boolean existsEnrollment(String courseId, String studentUsername) {
        return enrollmentRepository.existsByCourseIdAndStudentUsername(
                courseId,
                studentUsername
        );
    }
    public List<EnrollmentResponse> getAllEnrollments() {
        return enrollmentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<EnrollmentResponse> getEnrollmentsByTeacherUsername(String teacherUsername) {
        return enrollmentRepository.findByTeacherUsername(teacherUsername)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<EnrollmentResponse> getEnrollmentsByStudentUsername(String studentUsername) {
        return enrollmentRepository.findByStudentUsername(studentUsername)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public EnrollmentResponse updateStatus(
            String enrollmentId,
            UpdateEnrollmentStatusRequest request,
            String currentUsername,
            String currentRole
    ) {

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() ->
                        new EnrollmentNotFoundException("Enrollment not found"));

        if (!currentRole.equals("ADMIN")
                && !enrollment.getTeacherUsername().equals(currentUsername)) {

            throw new AccessDeniedException(
                    "You can update only enrollments for your own courses"
            );
        }

        EnrollmentStatus currentStatus = enrollment.getStatus();
        EnrollmentStatus newStatus = request.getStatus();

        validateStatusTransition(currentStatus, newStatus);

        enrollment.setStatus(newStatus);

        Enrollment updatedEnrollment = enrollmentRepository.save(enrollment);

        return mapToResponse(updatedEnrollment);
    }

    //status workflow
    private void validateStatusTransition(
            EnrollmentStatus currentStatus,
            EnrollmentStatus newStatus
    ) {

        if (currentStatus == newStatus) {
            throw new InvalidEnrollmentStatusException(
                    "Enrollment already has status " + currentStatus
            );
        }

        switch (currentStatus) {
            case PENDING -> {
                if (newStatus != EnrollmentStatus.APPROVED
                        && newStatus != EnrollmentStatus.REJECTED
                        && newStatus != EnrollmentStatus.CANCELLED) {
                    throw new InvalidEnrollmentStatusException(
                            "Invalid status transition from " + currentStatus + " to " + newStatus
                    );
                }
            }

            case APPROVED -> {
                if (newStatus != EnrollmentStatus.COMPLETED
                        && newStatus != EnrollmentStatus.CANCELLED) {
                    throw new InvalidEnrollmentStatusException(
                            "Invalid status transition from " + currentStatus + " to " + newStatus
                    );
                }
            }

            case REJECTED, COMPLETED, CANCELLED -> throw new InvalidEnrollmentStatusException(
                    "Cannot change status from final status " + currentStatus
            );
        }
    }

    public EnrollmentStatsResponse getStatsByCourseId(String courseId) {
        Long totalEnrollments = enrollmentRepository.countByCourseId(courseId);

        return new EnrollmentStatsResponse(
                courseId,
                totalEnrollments
        );
    }

    private EnrollmentResponse mapToResponse(Enrollment enrollment) {
        return EnrollmentResponse.builder()
                .id(enrollment.getId())
                .courseId(enrollment.getCourseId())
                .studentUsername(enrollment.getStudentUsername())
                .status(enrollment.getStatus())
                .teacherUsername(enrollment.getTeacherUsername())
                .createdAt(enrollment.getCreatedAt())
                .build();
    }
}