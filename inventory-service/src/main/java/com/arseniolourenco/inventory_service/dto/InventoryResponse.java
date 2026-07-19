package com.arseniolourenco.inventory_service.dto;

import lombok.Builder;

@Builder
public record InventoryResponse(
    String skuCode,
    boolean isInStock,
    int quantity
) {}
