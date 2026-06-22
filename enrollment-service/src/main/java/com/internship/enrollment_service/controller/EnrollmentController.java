package com.internship.enrollment_service.controller;

import com.internship.enrollment_service.dto.enrollment.EnrollmentResponse;
import com.internship.enrollment_service.dto.enrollment.EnrollmentStatsResponse;
import com.internship.enrollment_service.dto.enrollment.UpdateEnrollmentStatusRequest;
import com.internship.enrollment_service.service.EnrollmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<EnrollmentResponse> getAllEnrollments() {
        return enrollmentService.getAllEnrollments();
    }

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/student/me")
    public List<EnrollmentResponse> getMyStudentEnrollments(Authentication authentication) {
        return enrollmentService.getEnrollmentsByStudentUsername(
                authentication.getName()
        );
    }

    @PreAuthorize("hasRole('TEACHER')")
    @GetMapping("/teacher/me")
    public List<EnrollmentResponse> getMyTeacherEnrollments(Authentication authentication) {
        return enrollmentService.getEnrollmentsByTeacherUsername(
                authentication.getName()
        );
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @PatchMapping("/{id}/status")
    public EnrollmentResponse updateStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateEnrollmentStatusRequest request,
            Authentication authentication
    ) {

        String currentRole = authentication.getAuthorities()
                .stream()
                .findFirst()
                .orElseThrow()
                .getAuthority()
                .replace("ROLE_", "");

        return enrollmentService.updateStatus(
                id,
                request,
                authentication.getName(),
                currentRole
        );
    }

    @GetMapping("/course/{courseId}/stats")
    public EnrollmentStatsResponse getStatsByCourseId(@PathVariable String courseId) {
        return enrollmentService.getStatsByCourseId(courseId);
    }

    @GetMapping("/exists")
    public boolean existsEnrollment(@RequestParam String courseId,
                                    @RequestParam String studentUsername) {
        return enrollmentService.existsEnrollment(courseId, studentUsername);
    }

    @PreAuthorize("hasRole('STUDENT')")
    @DeleteMapping("/{id}")
    public void deleteMyEnrollment(
            @PathVariable String id,
            Authentication authentication
    ) {
        enrollmentService.deleteEnrollmentByStudent(
                id,
                authentication.getName()
        );
    }
}