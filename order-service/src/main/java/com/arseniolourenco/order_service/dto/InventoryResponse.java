package com.arseniolourenco.order_service.dto;

import lombok.Builder;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record InventoryResponse(
    String skuCode,
    @JsonProperty("inStock") boolean isInStock,
    Integer quantity
) {}
