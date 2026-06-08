package com.internship.course_service.service;

import com.internship.course_service.dto.auth.RegisterRequest;
import com.internship.course_service.dto.user.UpdateRoleRequest;
import com.internship.course_service.entity.User;
import com.internship.course_service.enums.Role;
import com.internship.course_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateRole(String userId, UpdateRoleRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setRole(request.getRole());

        return userRepository.save(user);
    }
}