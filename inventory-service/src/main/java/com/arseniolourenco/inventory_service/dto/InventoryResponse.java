package com.arseniolourenco.inventory_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record InventoryResponse(
    String skuCode,
    @JsonProperty("inStock") boolean isInStock,
    int quantity
) {}
