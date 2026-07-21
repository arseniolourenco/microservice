package com.arseniolourenco.order_service.mapper;

import com.arseniolourenco.order_service.dto.OrderLineItemsDto;
import com.arseniolourenco.order_service.dto.OrderRequest;
import com.arseniolourenco.order_service.dto.OrderResponse;
import com.arseniolourenco.order_service.model.Order;
import com.arseniolourenco.order_service.model.OrderLineItems;
import com.arseniolourenco.order_service.model.OutboxEvent;
import com.arseniolourenco.order_service.dto.OutboxEventDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "id", ignore = true)
    OutboxEvent toOutboxEvent(OutboxEventDto outboxEventDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orderNumber", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "message", ignore = true)
    @Mapping(source = "orderLineItemsDtoList", target = "orderLineItemsList")
    Order toOrder(OrderRequest orderRequest);

    OrderLineItems toOrderLineItems(OrderLineItemsDto orderLineItemsDto);

    @Mapping(source = "orderLineItemsList", target = "orderLineItemsDtoList")
    @Mapping(source = "status", target = "orderStatus")
    OrderResponse toOrderResponse(Order order);

    OrderLineItemsDto toOrderLineItemsDto(OrderLineItems orderLineItems);
    
    List<OrderLineItems> toOrderLineItemsList(List<OrderLineItemsDto> orderLineItemsDtoList);
    List<OrderLineItemsDto> toOrderLineItemsDtoList(List<OrderLineItems> orderLineItemsList);

    com.arseniolourenco.order_service.event.OrderPlacedEvent.OrderItemDto toOrderItemDto(OrderLineItems orderLineItems);
    List<com.arseniolourenco.order_service.event.OrderPlacedEvent.OrderItemDto> toOrderItemDtoList(List<OrderLineItems> orderLineItemsList);
}
