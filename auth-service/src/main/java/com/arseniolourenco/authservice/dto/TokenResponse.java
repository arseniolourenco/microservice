package com.arseniolourenco.authservice.dto;

import lombok.Builder;

@Builder
public record TokenResponse(
    String accessToken,
    String refreshToken,
    Integer expiresIn,
    Integer refreshExpiresIn,
    String tokenType
) {}
