package com.arseniolourenco.order_service.service;

import com.arseniolourenco.order_service.dto.OrderLineItemsDto;
import com.arseniolourenco.order_service.dto.OrderRequest;
import com.arseniolourenco.order_service.dto.OrderResponse;
import com.arseniolourenco.order_service.mapper.OrderMapper;
import com.arseniolourenco.order_service.model.OrderLineItems;
import com.arseniolourenco.order_service.model.OrderModel;
import com.arseniolourenco.order_service.model.OutboxEvent;
import com.arseniolourenco.order_service.repository.OrderRepository;
import com.arseniolourenco.order_service.repository.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.client.RestClient;

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

    @Mock
    private RestClient.Builder restClientBuilder;

    @Mock
    private KafkaTemplate kafkaTemplate ;

    @Mock
    private Tracer tracer;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    @Test
    void shouldPlaceOrder_AndCreateOutboxEvent() throws Exception {
        // Arrange
        OrderLineItemsDto itemDto = OrderLineItemsDto.builder()
            .skuCode("iphone_15")
            .price(BigDecimal.valueOf(1000))
            .quantity(1)
            .build();

        OrderRequest orderRequest = new OrderRequest(List.of(itemDto));

        OrderModel order = new OrderModel();
        order.setId(1L);
        order.setOrderNumber("12345");
        order.setStatus("PENDING");
        
        OrderLineItems item = new OrderLineItems();
        item.setSkuCode("iphone_15");
        item.setQuantity(1);
        item.setPrice(BigDecimal.valueOf(1000));
        order.setOrderLineItemsList(List.of(item));

        when(orderMapper.toOrder(orderRequest)).thenReturn(order);
        when(orderRepository.save(any(OrderModel.class))).thenReturn(order);
        when(orderMapper.toOrderItemDtoList(anyList())).thenReturn(List.of());
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(orderMapper.toOutboxEvent(any())).thenReturn(new OutboxEvent());
        
        OrderResponse orderResponse = new OrderResponse(
            "12345",
            "PENDING",
            List.of()
        );
        when(orderMapper.toOrderResponse(any(OrderModel.class))).thenReturn(orderResponse);

        // Act
        OrderResponse result = orderService.placeOrder(orderRequest);

        // Assert
        assertNotNull(result);
        verify(orderRepository).save(any(OrderModel.class));
        verify(outboxRepository).save(any(OutboxEvent.class));
        assertEquals("PENDING", result.orderStatus());
    }

    @Test
    void shouldUpdateOrderStatus() {
        // Arrange
        String orderNumber = "12345";
        OrderModel order = new OrderModel();
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
