package com.arseniolourenco.order_service.dto;

import lombok.Builder;

@Builder
public record InventoryRequest(
    String skuCode,
    Integer quantity
) {}