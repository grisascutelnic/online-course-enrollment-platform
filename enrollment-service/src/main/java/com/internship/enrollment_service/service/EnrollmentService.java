package com.internship.enrollment_service.service;

import com.internship.enrollment_service.dto.enrollment.EnrollmentResponse;
import com.internship.enrollment_service.dto.enrollment.UpdateEnrollmentStatusRequest;
import com.internship.enrollment_service.entity.Enrollment;
import com.internship.enrollment_service.enums.EnrollmentStatus;
import com.internship.enrollment_service.event.EnrollmentRequestedEvent;
import com.internship.enrollment_service.exception.EnrollmentNotFoundException;
import com.internship.enrollment_service.exception.InvalidEnrollmentStatusException;
import com.internship.enrollment_service.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;

    public Enrollment createEnrollmentFromEvent(EnrollmentRequestedEvent event) {

        boolean alreadyExists = enrollmentRepository
                .existsByStudentUsernameAndCourseId(
                        event.getStudentUsername(),
                        event.getCourseId()
                );

        if (alreadyExists) {
            throw new IllegalStateException("Student is already enrolled in this course");
        }

        Enrollment enrollment = Enrollment.builder()
                .courseId(event.getCourseId())
                .studentUsername(event.getStudentUsername())
                .status(EnrollmentStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        return enrollmentRepository.save(enrollment);
    }

    public List<EnrollmentResponse> getAllEnrollments() {
        return enrollmentRepository.findAll()
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
            UpdateEnrollmentStatusRequest request
    ) {

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() ->
                        new EnrollmentNotFoundException("Enrollment not found"));

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

    private EnrollmentResponse mapToResponse(Enrollment enrollment) {
        return EnrollmentResponse.builder()
                .id(enrollment.getId())
                .courseId(enrollment.getCourseId())
                .studentUsername(enrollment.getStudentUsername())
                .status(enrollment.getStatus())
                .createdAt(enrollment.getCreatedAt())
                .build();
    }
}