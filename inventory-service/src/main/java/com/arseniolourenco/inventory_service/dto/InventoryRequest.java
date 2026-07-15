package com.arseniolourenco.inventory_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InventoryRequest {
    @NotBlank(message = "SKU code cannot be blank")
    private String skuCode;
    @Min(value = 1, message = "Quantity must be greater than 0")
    private int quantity;
}