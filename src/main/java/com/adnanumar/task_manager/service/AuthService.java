package com.adnanumar.task_manager.service;

import com.adnanumar.task_manager.dto.request.LoginRequest;
import com.adnanumar.task_manager.dto.request.RegisterRequest;
import com.adnanumar.task_manager.dto.response.AuthResponse;
import com.adnanumar.task_manager.dto.response.UserResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    UserResponse getCurrentUser(String email);
}
