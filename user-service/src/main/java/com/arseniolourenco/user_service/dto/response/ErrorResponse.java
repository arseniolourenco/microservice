package com.arseniolourenco.user_service.dto.response;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record ErrorResponse(
    LocalDateTime timestamp,
    int status,
    String error,
    String message,
    String path
) {}
