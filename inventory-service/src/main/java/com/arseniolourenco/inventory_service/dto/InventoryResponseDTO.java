package com.arseniolourenco.inventory_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record InventoryResponseDTO(
    String skuCode,
    @JsonProperty("inStock") boolean isInStock,
    int quantity
) {}
