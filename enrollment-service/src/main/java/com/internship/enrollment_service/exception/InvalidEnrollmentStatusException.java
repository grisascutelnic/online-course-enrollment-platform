package com.internship.enrollment_service.exception;

public class InvalidEnrollmentStatusException extends RuntimeException {

    public InvalidEnrollmentStatusException(String message) {
        super(message);
    }
}