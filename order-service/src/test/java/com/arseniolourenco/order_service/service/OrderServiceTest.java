package com.arseniolourenco.order_service.service;

import com.arseniolourenco.order_service.dto.OrderLineItemsDto;
import com.arseniolourenco.order_service.dto.OrderRequest;
import com.arseniolourenco.order_service.model.Order;
import com.arseniolourenco.order_service.repository.OrderRepository;
import com.arseniolourenco.order_service.repository.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OutboxRepository outboxRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private OrderService orderService;

    @Test
    void shouldPlaceOrder_AndCreateOutboxEvent() throws Exception {
        // Arrange
        OrderLineItemsDto itemDto = new OrderLineItemsDto();
        itemDto.setSkuCode("iphone_15");
        itemDto.setPrice(BigDecimal.valueOf(1000));
        itemDto.setQuantity(1);

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setOrderLineItemsDtoList(List.of(itemDto));

        Order order = new Order();
        order.setOrderNumber("12345");
        order.setStatus("PENDING");

        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // Act
        Order result = orderService.placeOrder(orderRequest);

        // Assert
        assertNotNull(result);
        verify(orderRepository).save(any(Order.class));
        verify(outboxRepository).save(any());
        assertEquals("PENDING", result.getStatus());
    }

    @Test
    void shouldUpdateOrderStatus() {
        // Arrange
        String orderNumber = "12345";
        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setStatus("PENDING");

        when(orderRepository.findByOrderNumber(orderNumber)).thenReturn(Optional.of(order));

        // Act
        orderService.updateOrderStatus(orderNumber, "CONFIRMED", "Success");

        // Assert
        assertEquals("CONFIRMED", order.getStatus());
        assertEquals("Success", order.getMessage());
        verify(orderRepository).save(order);
    }
}
