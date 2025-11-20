package com.turnoapp.backend.service;

import com.turnoapp.backend.dto.auth.LoginRequest;
import com.turnoapp.backend.dto.auth.LoginResponse;
import com.turnoapp.backend.dto.auth.RegisterRequest;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    LoginResponse register(RegisterRequest request);
}
