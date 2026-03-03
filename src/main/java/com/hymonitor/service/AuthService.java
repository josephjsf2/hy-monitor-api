package com.hymonitor.service;

import com.hymonitor.dto.AuthResponse;
import com.hymonitor.dto.LoginRequest;
import com.hymonitor.dto.RegisterRequest;
import com.hymonitor.entity.AppUser;
import com.hymonitor.repository.AppUserRepository;
import com.hymonitor.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authentication service
 * Handles user registration and login
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Register a new user
     * @param request registration request
     * @return authentication response with JWT token
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");  // Will be replaced with custom exception in Task 10
        }

        AppUser user = AppUser.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .displayName(request.getDisplayName())
                .build();

        userRepository.save(user);

        LOGGER.info("User registered successfully: {}", user.getUsername());

        String token = jwtTokenProvider.generateToken(user.getUsername());

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .build();
    }

    /**
     * Authenticate user and generate JWT token
     * @param request login request
     * @return authentication response with JWT token
     */
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        String token = jwtTokenProvider.generateToken(request.getUsername());
        AppUser user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));  // Will be replaced with custom exception in Task 10

        LOGGER.info("User logged in successfully: {}", user.getUsername());

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .build();
    }
}
