package com.arseniolourenco.product_service;

import com.arseniolourenco.product_service.dto.ProductRequest;
import com.arseniolourenco.product_service.repository.ProductRepositoy;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class ProductServiceApplicationTests {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepositoy productRepositoy;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @AfterEach
    void tearDown() {
        productRepositoy.deleteAll(); // Clean up after each test
    }

    @Test
    void shouldCreateProduct() throws Exception {

        ProductRequest productRequest = getProductRequest();
        String productRequestString = objectMapper.writeValueAsString(productRequest);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productRequestString))
                .andExpect(status().isCreated());

        Assertions.assertEquals(1, productRepositoy.findAll().size());
    }


    //	@Test
//	void shouldGetProduct() throws Exception {
//		// Step 1: First create a product
//		ProductRequest productRequest = getProductRequest();
//		String productRequestString = objectMapper.writeValueAsString(productRequest);
//
//		// Perform POST to create product and assert creation
//		mockMvc.perform(MockMvcRequestBuilders.post("/api/product")
//						.contentType(MediaType.APPLICATION_JSON)
//						.content(productRequestString))
//				.andExpect(status().isCreated());
//
//		// Step 2: Retrieve the product ID from the repository
//		String productId = productRepositoy.findAll().get(0).getId();
//		System.out.println("Retrieved product ID: " + productId);  // Debugging log
//
//		// Perform GET request to fetch the created product by ID
//		mockMvc.perform(MockMvcRequestBuilders.get("/api/product/" + productId)
//						.contentType(MediaType.APPLICATION_JSON))
//				.andExpect(status().isOk()) // Assert that the response is OK
//				.andExpect(result -> {
//					// Optionally check the response content
//					String jsonResponse = result.getResponse().getContentAsString();
//					System.out.println("GET Response JSON: " + jsonResponse);  // Debugging log
//
//					ProductResponse productResponse = objectMapper.readValue(jsonResponse, ProductResponse.class);
//
//					// Assertions on the product data
//					Assertions.assertEquals("iPhpne 13", productResponse.getName());
//					Assertions.assertEquals("iPhone 13", productResponse.getDescription());
//				});
//	}
    private ProductRequest getProductRequest() {
        return ProductRequest.builder()
                .name("iPhpne 13")
                .description("iPhone 13")
                .price(BigDecimal.valueOf(1200))
                .build();
    }
}
