package com.arseniolourenco.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
public record TokenResponse(
    String accessToken,
    String refreshToken,
    Integer expiresIn,
    Integer refreshExpiresIn,
    String tokenType
) {}
