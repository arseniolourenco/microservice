package com.arseniolourenco.order_service.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OrderRequest {
    private final List<OrderLineItemsDto> orderLineItemsDtoList = new ArrayList<>();
}
