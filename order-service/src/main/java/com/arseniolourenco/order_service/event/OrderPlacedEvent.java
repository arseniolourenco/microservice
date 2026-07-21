package com.arseniolourenco.order_service.event;

import java.util.List;

public record OrderPlacedEvent(String orderNumber, List<OrderItemDto> items)
{
    public record OrderItemDto(String skuCode, Integer quantity) {}
}
