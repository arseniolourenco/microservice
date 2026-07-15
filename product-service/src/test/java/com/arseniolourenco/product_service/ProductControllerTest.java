package com.arseniolourenco.product_service;

import com.arseniolourenco.product_service.controller.ProductController;
import com.arseniolourenco.product_service.dto.ProductRequest;
import com.arseniolourenco.product_service.dto.ProductResponse;
import com.arseniolourenco.product_service.service.ProductService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@TestPropertySource(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.config.enabled=false"
})
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @Test
    void createProduct() throws Exception {
        ProductRequest request = new ProductRequest("iPhone 16", "iphone_16", "Latest iPhone", BigDecimal.valueOf(1600));
        ProductResponse response = new ProductResponse("1", "iPhone 16", "iphone_16", "Latest iPhone", BigDecimal.valueOf(1600));

        when(productService.createProduct(any(ProductRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("iPhone 16"))
                .andExpect(jsonPath("$.skuCode").value("iphone_16"));
    }

    @Test
    void getAllProducts() throws Exception {
        ProductResponse response = new ProductResponse("1", "iPhone 16", "iphone_16", "Latest iPhone", BigDecimal.valueOf(1600));
        
        when(productService.getAllProducts()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/products")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value("1"));
    }

    @Test
    void getProductById() throws Exception {
        ProductResponse response = new ProductResponse("1", "iPhone 16", "iphone_16", "Latest iPhone", BigDecimal.valueOf(1600));

        when(productService.getProductById("1")).thenReturn(response);

        mockMvc.perform(get("/api/products/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"));
    }

    @Test
    void updateProduct() throws Exception {
        ProductRequest request = new ProductRequest("iPhone 16 Pro", "iphone_16_pro", "Latest iPhone Pro", BigDecimal.valueOf(2000));
        ProductResponse response = new ProductResponse("1", "iPhone 16 Pro", "iphone_16_pro", "Latest iPhone Pro", BigDecimal.valueOf(2000));

        when(productService.updateProduct(eq("1"), any(ProductRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("iPhone 16 Pro"));
    }

    @Test
    void deleteProduct() throws Exception {
        doNothing().when(productService).deleteProduct("1");

        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());
    }
}