package com.arseniolourenco.inventory_service;

import com.arseniolourenco.inventory_service.dto.InventoryRequestDTO;
import com.arseniolourenco.inventory_service.repository.InventoryRepository;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@Tag("integration")
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false"
})
class InventoryIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("inventory_test")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InventoryRepository inventoryRepository;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        inventoryRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        inventoryRepository.deleteAll();
    }

    @Test
    void shouldAddStockAndCheckStock() throws Exception {
        // 1. Arrange - Prepare stock addition
        InventoryRequestDTO request = new InventoryRequestDTO("iphone_16", 10);
        String requestJson = objectMapper.writeValueAsString(List.of(request));

        // 2. Act - Add stock via API
        mockMvc.perform(post("/api/inventory/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk());

        // 3. Verify - In Database
        Assertions.assertEquals(1, inventoryRepository.count());
        Assertions.assertEquals(10, inventoryRepository.findAll().get(0).getQuantity());

        // 4. Act & Assert - Check stock via API
        mockMvc.perform(get("/api/inventory")
                        .param("skuCode", "iphone_16")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].skuCode").value("iphone_16"))
                .andExpect(jsonPath("$[0].inStock").value(true))
                .andExpect(jsonPath("$[0].quantity").value(10));
    }
}
