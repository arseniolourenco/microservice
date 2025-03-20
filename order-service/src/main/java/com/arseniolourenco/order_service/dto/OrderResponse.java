package com.arseniolourenco.order_service.dto;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {
    private String orderNumber;
    private String orderStatus;
    private List<OrderLineItemsDto> orderLineItemsDtoList;

}