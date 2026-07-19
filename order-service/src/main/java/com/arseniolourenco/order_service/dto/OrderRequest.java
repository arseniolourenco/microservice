package com.arseniolourenco.order_service.dto;

import java.util.List;

public record OrderRequest(
    List<OrderLineItemsDto> orderLineItemsDtoList
) {}
