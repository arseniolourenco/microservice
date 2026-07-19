package com.arseniolourenco.inventory_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
public record InventoryResponse(
    String skuCode,
    boolean isInStock,
    int quantity
) {}
