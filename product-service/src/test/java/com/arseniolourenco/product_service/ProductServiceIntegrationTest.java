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
class ProductServiceIntegrationTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:latest"); // ✅ Removed withReuse(true)
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ProductRepository productRepository;

    @BeforeAll
    static void startContainer() {
        mongoDBContainer.start();
    }

    @AfterAll
    static void stopContainer() {
        mongoDBContainer.stop();
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @BeforeEach
    void setUp() {
        System.out.println("Starting test...");
    }

    @AfterEach
    void tearDown() {
        productRepository.deleteAll();
        System.out.println("Test completed.");
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
        Assertions.assertEquals(1, productRepository.count()); // ✅ Used count() instead of findAll().size()

        Product savedProduct = productRepository.findAll().get(0);
        Assertions.assertEquals("iPhone 16", savedProduct.getName());
        Assertions.assertEquals("The latest Apple iPhone", savedProduct.getDescription());
        Assertions.assertEquals(BigDecimal.valueOf(1600), savedProduct.getPrice());
    }


//    @Test
//    void shouldRetrieveProduct() throws Exception {
//        // Arrange
//        ProductRequest productRequest = createProductRequest();
//        Product product = Product.builder()
//                .name(productRequest.getName())
//                .description(productRequest.getDescription())
//                .price(productRequest.getPrice())
//                .build();
//
//        // Save the product
//        Product savedProduct = productRepository.save(product);
//
//        // Act & Assert
//        mockMvc.perform(MockMvcRequestBuilders.get("/api/product/" + savedProduct.getId())
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(result -> {
//                    // Validate JSON response
//                    String jsonResponse = result.getResponse().getContentAsString();
//                    ProductResponse productResponse = objectMapper.readValue(jsonResponse, ProductResponse.class);
//
//                    Assertions.assertEquals(productRequest.getName(), productResponse.getName(), "Product name does not match");
//                    Assertions.assertEquals(productRequest.getDescription(), productResponse.getDescription(), "Product description does not match");
//                    Assertions.assertEquals(productRequest.getPrice(), productResponse.getPrice(), "Product price does not match");
//                });
//    }

    @Test
    public void shouldGetProductById() throws Exception {
        // Arrange - Save a product
        ProductRequest productRequest = new ProductRequest("Laptop", "High-end gaming laptop", BigDecimal.valueOf(1500));
        Product savedProduct = productRepository.save(new Product(null, productRequest.getName(), productRequest.getDescription(), productRequest.getPrice()));

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/api/product/" + savedProduct.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String jsonResponse = result.getResponse().getContentAsString();
                    ProductResponse productResponse = objectMapper.readValue(jsonResponse, ProductResponse.class);

                    Assertions.assertEquals(productRequest.getName(), productResponse.getName(), "Product name does not match");
                    Assertions.assertEquals(productRequest.getDescription(), productResponse.getDescription(), "Product description does not match");
                    Assertions.assertEquals(productRequest.getPrice(), productResponse.getPrice(), "Product price does not match");
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