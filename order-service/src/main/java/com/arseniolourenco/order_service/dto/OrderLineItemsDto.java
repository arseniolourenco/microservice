package com.arseniolourenco.order_service.dto;

import lombok.Builder;
import java.math.BigDecimal;

@Builder
public record OrderLineItemsDto(
    Long id,
    String skuCode,
    BigDecimal price,
    Integer quantity
) {}
