package com.arseniolourenco.inventory_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record InventoryRequest(
    @NotBlank(message = "SKU code cannot be blank")
    String skuCode,
    
    @Min(value = 1, message = "Quantity must be greater than 0")
    int quantity
) {}