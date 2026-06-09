package com.internship.enrollment_service.dto.enrollment;

import com.internship.enrollment_service.enums.EnrollmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEnrollmentStatusRequest {

    @NotNull(message = "Status is required")
    private EnrollmentStatus status;
}