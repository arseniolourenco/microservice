package com.arseniolourenco.inventory_service.mapper;

import com.arseniolourenco.inventory_service.dto.InventoryResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InventoryMapper {

    default InventoryResponse toInventoryResponse(String skuCode, boolean isInStock, int quantity) {
        return new InventoryResponse(skuCode, isInStock, quantity);
    }
}
