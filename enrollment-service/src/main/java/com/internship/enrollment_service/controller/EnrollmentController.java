package com.internship.enrollment_service.controller;

import com.internship.enrollment_service.dto.enrollment.EnrollmentResponse;
import com.internship.enrollment_service.dto.enrollment.UpdateEnrollmentStatusRequest;
import com.internship.enrollment_service.service.EnrollmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @GetMapping
    public List<EnrollmentResponse> getAllEnrollments() {
        return enrollmentService.getAllEnrollments();
    }

    @GetMapping("/student/{studentUsername}")
    public List<EnrollmentResponse> getEnrollmentsByStudentUsername(
            @PathVariable String studentUsername
    ) {
        return enrollmentService.getEnrollmentsByStudentUsername(studentUsername);
    }

    @PatchMapping("/{id}/status")
    public EnrollmentResponse updateStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateEnrollmentStatusRequest request
    ) {
        return enrollmentService.updateStatus(id, request);
    }
}