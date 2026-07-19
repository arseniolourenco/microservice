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
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false"
})
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
        mockMvc.perform(MockMvcRequestBuilders.post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productRequestJson))
                .andExpect(status().isCreated());

        // Assert
        Assertions.assertEquals(1, productRepository.count()); // ✅ Used count() instead of findAll().size()

        Product savedProduct = productRepository.findAll().get(0);
        Assertions.assertEquals("iPhone 16", savedProduct.getName());
        Assertions.assertEquals("iphone_16", savedProduct.getSkuCode());
        Assertions.assertEquals("The latest Apple iPhone", savedProduct.getDescription());
        Assertions.assertEquals(0, BigDecimal.valueOf(1600).compareTo(savedProduct.getPrice()));
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
        ProductRequest productRequest = new ProductRequest("Laptop", "laptop_001", "High-end gaming laptop", BigDecimal.valueOf(1500));
        Product savedProduct = productRepository.save(Product.builder()
                .name(productRequest.name())
                .skuCode(productRequest.skuCode())
                .description(productRequest.description())
                .price(productRequest.price())
                .build());

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/api/products/" + savedProduct.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String jsonResponse = result.getResponse().getContentAsString();
                    ProductResponse productResponse = objectMapper.readValue(jsonResponse, ProductResponse.class);

                    Assertions.assertEquals(productRequest.name(), productResponse.name(), "Product name does not match");
                    Assertions.assertEquals(productRequest.skuCode(), productResponse.skuCode(), "Product SKU does not match");
                    Assertions.assertEquals(productRequest.description(), productResponse.description(), "Product description does not match");
                    Assertions.assertEquals(0, productRequest.price().compareTo(productResponse.price()), "Product price does not match");
                });
    }

    @Test
    public void shouldGetAllProducts() throws Exception {
        // Arrange
        productRepository.save(Product.builder().name("Laptop").skuCode("laptop_001").description("Gaming laptop").price(BigDecimal.valueOf(1500)).build());
        productRepository.save(Product.builder().name("Phone").skuCode("phone_001").description("Smartphone").price(BigDecimal.valueOf(800)).build());

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/api/products")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String jsonResponse = result.getResponse().getContentAsString();
                    ProductResponse[] productResponses = objectMapper.readValue(jsonResponse, ProductResponse[].class);
                    Assertions.assertEquals(2, productResponses.length);
                });
    }

    @Test
    public void shouldUpdateProduct() throws Exception {
        // Arrange
        Product savedProduct = productRepository.save(Product.builder().name("Laptop").skuCode("laptop_001").description("Gaming laptop").price(BigDecimal.valueOf(1500)).build());
        ProductRequest updateRequest = new ProductRequest("Laptop Pro", "laptop_001", "Gaming laptop Pro", BigDecimal.valueOf(2000));
        String updateRequestJson = objectMapper.writeValueAsString(updateRequest);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.put("/api/products/" + savedProduct.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequestJson))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String jsonResponse = result.getResponse().getContentAsString();
                    ProductResponse productResponse = objectMapper.readValue(jsonResponse, ProductResponse.class);
                    Assertions.assertEquals("Laptop Pro", productResponse.name());
                    Assertions.assertEquals(0, BigDecimal.valueOf(2000).compareTo(productResponse.price()));
                });

        Product updatedProduct = productRepository.findById(savedProduct.getId()).orElseThrow();
        Assertions.assertEquals("Laptop Pro", updatedProduct.getName());
    }

    @Test
    public void shouldDeleteProduct() throws Exception {
        // Arrange
        Product savedProduct = productRepository.save(Product.builder().name("Laptop").skuCode("laptop_001").description("Gaming laptop").price(BigDecimal.valueOf(1500)).build());

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/products/" + savedProduct.getId()))
                .andExpect(status().isNoContent());

        Assertions.assertTrue(productRepository.findById(savedProduct.getId()).isEmpty());
    }

    private ProductRequest createProductRequest() {
        return new ProductRequest(
                "iPhone 16",
                "iphone_16",
                "The latest Apple iPhone",
                BigDecimal.valueOf(1600)
        );
    }
}