package com.adnanumar.task_manager.service.impl;

import com.adnanumar.task_manager.dto.request.LoginRequest;
import com.adnanumar.task_manager.dto.request.RegisterRequest;
import com.adnanumar.task_manager.dto.response.AuthResponse;
import com.adnanumar.task_manager.dto.response.UserResponse;
import com.adnanumar.task_manager.entity.User;
import com.adnanumar.task_manager.error.BadRequestException;
import com.adnanumar.task_manager.error.ResourceNotFoundException;
import com.adnanumar.task_manager.repository.UserRepository;
import com.adnanumar.task_manager.security.AuthUtil;
import com.adnanumar.task_manager.service.AuthService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthServiceImpl implements AuthService {

    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    AuthUtil authUtil;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.email());

        if (userRepository.existsByEmail(request.email())) {
            log.warn("Registration failed — email already exists: {}", request.email());
            throw new BadRequestException("Email is already registered: " + request.email());
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .globalRole(request.globalRole())
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully — id: {}, email: {}", savedUser.getId(), savedUser.getEmail());

        String token = authUtil.generateAccessToken(savedUser);
        return new AuthResponse(token, savedUser.getEmail(), savedUser.getName(), savedUser.getGlobalRole().name());
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.email());

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    log.warn("Login failed — no user found with email: {}", request.email());
                    return new BadRequestException("Invalid email or password");
                });

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            log.warn("Login failed — wrong password for email: {}", request.email());
            throw new BadRequestException("Invalid email or password");
        }

        log.info("Login successful for user id: {}, email: {}", user.getId(), user.getEmail());
        String token = authUtil.generateAccessToken(user);
        return new AuthResponse(token, user.getEmail(), user.getName(), user.getGlobalRole().name());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String email) {
        log.info("Fetching current user profile for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Current user not found for email: {}", email);
                    return new ResourceNotFoundException("User", email);
                });

        log.debug("Returning profile for user: {}", user.getEmail());
        return UserResponse.fromEntity(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> searchUsers(String query) {
        log.info("Searching for users with query: '{}'", query);
        return userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query)
                .stream()
                .map(UserResponse::fromEntity)
                .toList();
    }
}
