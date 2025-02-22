package com.arseniolourenco.product_service;

import com.arseniolourenco.product_service.dto.ProductRequest;
import com.arseniolourenco.product_service.dto.ProductResponse;
import com.arseniolourenco.product_service.model.Product;
import com.arseniolourenco.product_service.repository.ProductRepository;
import com.arseniolourenco.product_service.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void shouldCreateProduct() {
        // Arrange
        ProductRequest productRequest = new ProductRequest("Laptop", "Gaming Laptop", BigDecimal.valueOf(1200.00));
        Product savedProduct = new Product("123", "Laptop", "Gaming Laptop", BigDecimal.valueOf(1200.00));
        ProductResponse expectedResponse = new ProductResponse("123", "Laptop", "Gaming Laptop", BigDecimal.valueOf(1200.00));

        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // Act
        ProductResponse response = productService.createProduct(productRequest);

        // Assert
        assertNotNull(response);
        assertEquals(expectedResponse.getId(), response.getId());
        assertEquals(expectedResponse.getName(), response.getName());
        assertEquals(expectedResponse.getDescription(), response.getDescription());
        assertEquals(expectedResponse.getPrice(), response.getPrice());
    }

    @Test
    void shouldRetrieveAllProducts() {
        // Arrange: Mock the repository response
        List<Product> mockProducts = Arrays.asList(
                new Product("1", "Laptop", "Gaming Laptop", BigDecimal.valueOf(1200.00)),
                new Product("2", "Phone", "Smartphone", BigDecimal.valueOf(800.00))
        );

        when(productRepository.findAll()).thenReturn(mockProducts);

        // Act: Call the service method
        List<ProductResponse> responses = productService.getAllProducts();

        // Assert: Validate response
        assertNotNull(responses);
        assertEquals(2, responses.size());

        assertEquals("Laptop", responses.get(0).getName());
        assertEquals("1", responses.get(0).getId());
        assertEquals(BigDecimal.valueOf(1200.00), responses.get(0).getPrice());

        assertEquals("Phone", responses.get(1).getName());
        assertEquals("2", responses.get(1).getId());
        assertEquals(BigDecimal.valueOf(800.00), responses.get(1).getPrice());

        // Verify interactions
        verify(productRepository, times(1)).findAll();

    }

    @Test
    void shouldRetrieveProductById() {
        // Arrange: Mock the repository response
        String productId = "123";
        Product mockProduct = new Product(productId, "Laptop", "Gaming Laptop", BigDecimal.valueOf(1200.00));
        ProductResponse expectedResponse = new ProductResponse(productId, "Laptop", "Gaming Laptop", BigDecimal.valueOf(1200.00));

        when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));

        // Act: Call the service method
        ProductResponse response = productService.getProductById(productId);

        // Assert: Validate response
        assertNotNull(response);
        assertEquals(expectedResponse.getId(), response.getId());
        assertEquals(expectedResponse.getName(), response.getName());
        assertEquals(expectedResponse.getDescription(), response.getDescription());
        assertEquals(expectedResponse.getPrice(), response.getPrice());

        // Verify interactions
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    void getProductById_WhenProductNotFound_ShouldThrowException() {
        // Arrange
        String productId = "999";

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                productService.getProductById(productId)
        );

        assertEquals("Product not found with ID: 999", exception.getMessage());

        // Verify that `findById()` was called once
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    void shouldUpdateProduct() {
        // Arrange (Preparação)
        String productId = "123";

        Product existingProduct = new Product(productId, "Laptop", "Gaming Laptop", BigDecimal.valueOf(800.00));
        ProductRequest updatedRequest = new ProductRequest("Laptop", "Gaming Laptop!", BigDecimal.valueOf(1200.00));
        Product updatedProduct = new Product(productId, "Laptop Macbook Pro", "Laptop 2025", BigDecimal.valueOf(1200.00));

        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        // Act (Ação)
        ProductResponse response = productService.updateProduct(productId, updatedRequest);

        // Assert (Verificação)
        assertNotNull(response);
        assertEquals("Laptop Macbook Pro", response.getName());
        assertEquals("Laptop 2025", response.getDescription());
        assertEquals(BigDecimal.valueOf(1200.00), response.getPrice());

        verify(productRepository).findById(productId);
        verify(productRepository).save(any(Product.class));
    }


    @Test
    void shouldDeleteProduct_Success() {
        // Arrange
        String productId = "123";
        Product product = new Product(productId, "Laptop", "Gaming Laptop", BigDecimal.valueOf(1200.00));

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // Act
        productService.deleteProduct(productId);

        // Assert
        verify(productRepository).deleteById(productId);
    }

    @Test
    void deleteProduct_WhenProductNotFound_ShouldThrowException() {
        // Arrange
        String productId = "999";

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                productService.deleteProduct(productId)
        );

        assertEquals("Product not found with ID: 999", exception.getMessage());

        // Ensure deleteById is never called
        verify(productRepository, never()).deleteById(anyString());
    }
}