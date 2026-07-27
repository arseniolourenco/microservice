package com.arseniolourenco.inventory_service.mapper;

import com.arseniolourenco.inventory_service.dto.InventoryResponseDTO;
import com.arseniolourenco.inventory_service.event.OrderPlacedEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.arseniolourenco.inventory_service.dto.InventoryRequestDTO;
import com.arseniolourenco.inventory_service.model.InventoryModel;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InventoryMapper {

    @Mapping(target = "isInStock", expression = "java(inventory.getQuantity() > 0)")
    InventoryResponseDTO toInventoryResponse(InventoryModel inventory);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    InventoryModel toInventory(InventoryRequestDTO inventoryRequestDto);

    @Mapping(source = "skuCode", target = "skuCode")
    @Mapping(source = "quantity", target = "quantity")
    InventoryRequestDTO toInventoryRequest(OrderPlacedEvent.OrderItemDto orderItemDto);
    
    List<InventoryRequestDTO> toInventoryRequestList(List<OrderPlacedEvent.OrderItemDto> orderItemDtoList);
}
