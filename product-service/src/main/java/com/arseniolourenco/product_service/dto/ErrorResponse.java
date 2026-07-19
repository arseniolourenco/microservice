package com.arseniolourenco.product_service.dto;

import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record ErrorResponse(
    LocalDateTime timestamp,
    int status,
    String error,
    String message,
    String path,
    List<String> errors // ✅ List of field-specific errors (optional)
) {}
