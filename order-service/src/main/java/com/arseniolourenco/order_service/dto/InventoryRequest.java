package com.arseniolourenco.order_service.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryRequest {
    private String skuCode;
    private Integer quantity;
}