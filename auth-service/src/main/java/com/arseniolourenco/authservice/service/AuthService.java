package com.arseniolourenco.authservice.service;

import com.arseniolourenco.authservice.dto.LoginRequest;
import com.arseniolourenco.authservice.dto.RegisterRequest;
import com.arseniolourenco.authservice.dto.TokenResponse;

public interface AuthService {
    TokenResponse login(LoginRequest request);
    void register(RegisterRequest request);
}
