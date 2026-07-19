package com.arseniolourenco.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record InventoryResponse(
    String skuCode,
    @JsonProperty("inStock") boolean isInStock,
    Integer quantity
) {}
