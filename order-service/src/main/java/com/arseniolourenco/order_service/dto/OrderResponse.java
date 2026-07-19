package com.arseniolourenco.order_service.dto;

import lombok.Builder;
import java.util.List;

@Builder
public record OrderResponse(
    String orderNumber,
    String orderStatus,
    List<OrderLineItemsDto> orderLineItemsDtoList
) {}