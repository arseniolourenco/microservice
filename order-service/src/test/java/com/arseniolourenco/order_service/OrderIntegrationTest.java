package com.arseniolourenco.order_service;

import com.arseniolourenco.order_service.dto.OrderLineItemsDto;
import com.arseniolourenco.order_service.dto.OrderRequest;
import com.arseniolourenco.order_service.repository.OrderRepository;
import com.arseniolourenco.order_service.repository.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@Tag("integration")
class OrderIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("order_test")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OutboxRepository outboxRepository;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        // Disable Kafka during this test to avoid connection errors if Kafka container isn't used
        registry.add("spring.kafka.bootstrap-servers", () -> "localhost:9092"); 
    }

    @AfterEach
    void tearDown() {
        outboxRepository.deleteAll();
        orderRepository.deleteAll();
    }

    @Test
    void shouldPlaceOrderAndSaveOutboxEvent() throws Exception {
        // 1. Arrange
        OrderLineItemsDto item = OrderLineItemsDto.builder()
            .skuCode("iphone_15")
            .price(BigDecimal.valueOf(1000))
            .quantity(1)
            .build();

        OrderRequest request = new OrderRequest(List.of(item));

        String requestJson = objectMapper.writeValueAsString(request);

        // 2. Act
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated());

        // 3. Assert
        Assertions.assertEquals(1, orderRepository.count());
        Assertions.assertEquals(1, outboxRepository.count());
        Assertions.assertEquals("OrderCreated", outboxRepository.findAll().getFirst().getEventType());
        Assertions.assertEquals("NEW", outboxRepository.findAll().getFirst().getStatus());
    }
}
