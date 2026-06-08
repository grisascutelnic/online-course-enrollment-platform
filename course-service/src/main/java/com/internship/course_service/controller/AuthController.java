package com.internship.course_service.controller;

import com.internship.course_service.dto.auth.AuthResponse;
import com.internship.course_service.dto.auth.LoginRequest;
import com.internship.course_service.dto.auth.RegisterRequest;
import com.internship.course_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor // generates a constructor for all final fields.
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request //la Valid intoarce 400 Bad Request daca nu corespudne validarilor DTO
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.register(request));
    } //ResponseEntity it's used to return a status

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {

        return ResponseEntity.ok(
                authService.login(request)
        );
    }
}