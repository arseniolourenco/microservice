package com.arseniolourenco.product_service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ProductRequest(
    @NotBlank(message = "Name is required") String name,
    @NotBlank(message = "SKU Code is required") String skuCode,
    @NotBlank(message = "Description is required") String description,
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0") BigDecimal price
) {}