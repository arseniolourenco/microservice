package com.arseniolourenco.user_service.dto.response;

import java.time.LocalDateTime;

import com.arseniolourenco.user_service.model.Role;

public record UserResponse(
        Long id,
        String firstname,
        String lastname,
        String email,
        Role role,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}