package com.arseniolourenco.product_service;

import com.arseniolourenco.product_service.dto.ProductRequest;
import com.arseniolourenco.product_service.dto.ProductResponse;
import com.arseniolourenco.product_service.model.Product;
import com.arseniolourenco.product_service.repository.ProductRepository;
import com.arseniolourenco.product_service.service.ProductService;
import com.arseniolourenco.product_service.mapper.ProductMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    private static final String PRODUCT_ID = "123";
    private static final String INVALID_PRODUCT_ID = "999";

    @Mock
    private ProductRepository repository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService service;

    @Test
    void shouldCreateProduct() {

        ProductRequest request = createRequest();
        Product product = createProduct();
        ProductResponse expected = createResponse();

        when(productMapper.mapToProduct(any(ProductRequest.class))).thenReturn(product);
        when(repository.save(any(Product.class))).thenReturn(product);
        when(productMapper.mapToProductResponse(any(Product.class))).thenReturn(expected);

        ProductResponse actual = service.createProduct(request);

        assertEquals(expected, actual);

        verify(productMapper).mapToProduct(any(ProductRequest.class));
        verify(repository).save(any(Product.class));
        verify(productMapper).mapToProductResponse(any(Product.class));
    }

    @Test
    void shouldReturnAllProducts() {

        Product product1 = createProduct();
        Product product2 = Product.builder()
                .id("2")
                .name("Phone")
                .skuCode("phone_001")
                .description("Smartphone")
                .price(BigDecimal.valueOf(800))
                .build();

        ProductResponse response1 = createResponse();
        ProductResponse response2 = new ProductResponse(
                "2",
                "Phone",
                "phone_001",
                "Smartphone",
                BigDecimal.valueOf(800)
        );

        when(repository.findAll()).thenReturn(List.of(product1, product2));
        when(productMapper.mapToProductResponse(product1)).thenReturn(response1);
        when(productMapper.mapToProductResponse(product2)).thenReturn(response2);

        List<ProductResponse> responses = service.getAllProducts();

        assertEquals(2, responses.size());

        assertEquals(response1, responses.get(0));
        assertEquals(response2, responses.get(1));

        verify(repository).findAll();
        verify(productMapper).mapToProductResponse(product1);
        verify(productMapper).mapToProductResponse(product2);
    }

    @Test
    void shouldReturnProductById() {

        Product product = createProduct();
        ProductResponse response = createResponse();

        when(repository.findById(PRODUCT_ID))
                .thenReturn(Optional.of(product));
        when(productMapper.mapToProductResponse(product)).thenReturn(response);

        ProductResponse actual = service.getProductById(PRODUCT_ID);

        assertEquals(response, actual);

        verify(repository).findById(PRODUCT_ID);
        verify(productMapper).mapToProductResponse(product);
    }

    @Test
    void shouldThrowExceptionWhenProductDoesNotExist() {

        when(repository.findById(INVALID_PRODUCT_ID))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> service.getProductById(INVALID_PRODUCT_ID)
        );

        assertEquals(
                "Product With ID: 999 Not Found",
                exception.getMessage()
        );

        verify(repository).findById(INVALID_PRODUCT_ID);
    }

    @Test
    void shouldUpdateProduct() {

        Product existing = createProduct();

        ProductRequest request = new ProductRequest(
                "MacBook Pro",
                "mac_001",
                "MacBook Pro M4",
                BigDecimal.valueOf(2500)
        );

        Product updated = Product.builder()
                .id(PRODUCT_ID)
                .name("MacBook Pro")
                .skuCode("mac_001")
                .description("MacBook Pro M4")
                .price(BigDecimal.valueOf(2500))
                .build();

        ProductResponse expected = new ProductResponse(
                PRODUCT_ID,
                "MacBook Pro",
                "mac_001",
                "MacBook Pro M4",
                BigDecimal.valueOf(2500)
        );

        when(repository.findById(PRODUCT_ID))
                .thenReturn(Optional.of(existing));
        when(repository.save(any(Product.class)))
                .thenReturn(updated);
        when(productMapper.mapToProductResponse(updated)).thenReturn(expected);

        ProductResponse actual = service.updateProduct(PRODUCT_ID, request);

        assertEquals(expected, actual);

        verify(repository).findById(PRODUCT_ID);
        verify(repository).save(any(Product.class));
        verify(productMapper).mapToProductResponse(updated);
    }

    @Test
    void shouldDeleteProduct() {

        when(repository.findById(PRODUCT_ID))
                .thenReturn(Optional.of(createProduct()));

        service.deleteProduct(PRODUCT_ID);

        verify(repository).delete(any(Product.class));
    }

    @Test
    void shouldThrowExceptionWhenDeletingUnknownProduct() {

        when(repository.findById(INVALID_PRODUCT_ID))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> service.deleteProduct(INVALID_PRODUCT_ID)
        );

        assertEquals(
                "Product With ID: 999 Not Found",
                exception.getMessage()
        );

        verify(repository).findById(INVALID_PRODUCT_ID);
        verify(repository, never()).deleteById(anyString());
    }

    // -------------------------------------------------------------------------
    // Factory Methods
    // -------------------------------------------------------------------------

    private ProductRequest createRequest() {
        return new ProductRequest(
                "Laptop",
                "laptop_001",
                "Gaming Laptop",
                BigDecimal.valueOf(1200)
        );
    }

    private Product createProduct() {
        return Product.builder()
                .id(PRODUCT_ID)
                .name("Laptop")
                .skuCode("laptop_001")
                .description("Gaming Laptop")
                .price(BigDecimal.valueOf(1200))
                .build();
    }

    private ProductResponse createResponse() {
        return new ProductResponse(
                PRODUCT_ID,
                "Laptop",
                "laptop_001",
                "Gaming Laptop",
                BigDecimal.valueOf(1200)
        );
    }
}