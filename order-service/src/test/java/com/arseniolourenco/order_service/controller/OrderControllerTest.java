package com.arseniolourenco.order_service.controller;

import com.arseniolourenco.order_service.dto.OrderLineItemsDto;
import com.arseniolourenco.order_service.dto.OrderRequest;
import com.arseniolourenco.order_service.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@TestPropertySource(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.config.enabled=false"
})
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @Test
    void shouldPlaceOrder() throws Exception {
        // Arrange
        OrderLineItemsDto item = OrderLineItemsDto.builder()
            .skuCode("iphone_15")
            .price(BigDecimal.valueOf(1000))
            .quantity(1)
            .build();

        OrderRequest request = new OrderRequest(List.of(item));

        com.arseniolourenco.order_service.dto.OrderResponse order = new com.arseniolourenco.order_service.dto.OrderResponse(
            UUID.randomUUID().toString(),
            "PENDING",
            List.of()
        );

        when(orderService.placeOrder(any(OrderRequest.class))).thenReturn(order);

        String requestJson = objectMapper.writeValueAsString(request);

        // Act & Assert
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated());
    }
}
