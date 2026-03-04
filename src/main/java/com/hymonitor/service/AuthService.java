package com.hymonitor.service;

import com.hymonitor.dto.AuthResponse;
import com.hymonitor.dto.LoginRequest;
import com.hymonitor.dto.RegisterRequest;
import com.hymonitor.entity.AppUser;
import com.hymonitor.exception.DuplicateResourceException;
import com.hymonitor.exception.ResourceNotFoundException;
import com.hymonitor.repository.AppUserRepository;
import com.hymonitor.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("Username already exists");
        }

        AppUser user = AppUser.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .displayName(request.displayName())
                .build();

        userRepository.save(user);

        LOGGER.info("User registered successfully: {}", user.getUsername());

        String token = jwtTokenProvider.generateToken(user.getUsername());

        return new AuthResponse(token, user.getUsername(), user.getDisplayName());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        String token = jwtTokenProvider.generateToken(request.username());
        AppUser user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        LOGGER.info("User logged in successfully: {}", user.getUsername());

        return new AuthResponse(token, user.getUsername(), user.getDisplayName());
    }
}
