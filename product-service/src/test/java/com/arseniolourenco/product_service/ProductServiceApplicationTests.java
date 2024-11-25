package com.arseniolourenco.product_service;

import com.arseniolourenco.product_service.dto.ProductRequest;
import com.arseniolourenco.product_service.dto.ProductResponse;
import com.arseniolourenco.product_service.model.Product;
import com.arseniolourenco.product_service.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
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

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:latest");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @BeforeEach
    void showUp(){
        productRepository.findAll();    // Clean up after each test
    }

    @AfterEach
    void tearDown() {
        productRepository.deleteAll();  // Clean up after each test
    }

    @Test
    void shouldCreateProduct() throws Exception {
        // Arrange
        ProductRequest productRequest = createProductRequest();
        String productRequestJson = objectMapper.writeValueAsString(productRequest);

        // Act
        mockMvc.perform(MockMvcRequestBuilders.post("/api/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productRequestJson))
                .andExpect(status().isCreated());

        // Assert
        Assertions.assertEquals( 1, productRepository.findAll().size());
    }

    @Test
    void shouldRetrieveProduct() throws Exception {
        // Step 1: Use createProductRequest() to define the product
        ProductRequest productRequest = createProductRequest();
        Product product = Product.builder().name(productRequest.getName()).description(productRequest.getDescription()).price(productRequest.getPrice()).build();

        // Step 2: Save the product directly to the repository
        productRepository.save(product);

        // Step 3: Perform GET request to fetch the saved product
        mockMvc.perform(MockMvcRequestBuilders.get("/api/product/id/" + product.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String jsonResponse = result.getResponse().getContentAsString();
                    ProductResponse productResponse = objectMapper.readValue(jsonResponse, ProductResponse.class);

            // Assertions to validate the response
            Assertions.assertEquals(productRequest.getName(), productResponse.getName());
            Assertions.assertEquals(productRequest.getDescription(), productResponse.getDescription());
            Assertions.assertEquals(productRequest.getPrice(), productResponse.getPrice());
        });
    }

    private ProductRequest createProductRequest() {
        return ProductRequest.builder()
                .name("iPhone 16")
                .description("The latest Apple iPhone")
                .price(BigDecimal.valueOf(1600))
                .build();
    }
}