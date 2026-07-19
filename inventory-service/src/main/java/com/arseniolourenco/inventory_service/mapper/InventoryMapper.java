package com.arseniolourenco.inventory_service.mapper;

import com.arseniolourenco.inventory_service.dto.InventoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.arseniolourenco.inventory_service.dto.InventoryRequest;
import com.arseniolourenco.inventory_service.model.Inventory;

@Mapper(componentModel = "spring")
public interface InventoryMapper {

    @Mapping(target = "isInStock", expression = "java(inventory.getQuantity() > 0)")
    InventoryResponse toInventoryResponse(Inventory inventory);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    Inventory toInventory(InventoryRequest inventoryRequest);
}
