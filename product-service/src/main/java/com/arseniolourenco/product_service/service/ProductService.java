package com.arseniolourenco.product_service.service;

import com.arseniolourenco.product_service.dto.ProductRequest;
import com.arseniolourenco.product_service.dto.ProductResponse;
import com.arseniolourenco.product_service.mapper.ProductMapper;
import com.arseniolourenco.product_service.model.Product;
import com.arseniolourenco.product_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    @Autowired
    private final ProductRepository productRepository;

//    public Product createProduct(ProductRequest productRequest) {
//        Product product = Product.builder()
//                .name(productRequest.getName())
//                .description(productRequest.getDescription())
//                .price(productRequest.getPrice())
//                .build();
//
//        Product savedProduct = productRepository.save(product);
//        log.info("Product with ID {} has been successfully created!", savedProduct.getId());
//
//        return savedProduct;
//    }

    public Product createProduct(ProductRequest productRequest) {
        // Step 1: Validate the request
        if (productRequest.getName() == null || productRequest.getName().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be null or empty");
        }

        // Step 2: Map the DTO to an entity
        Product product = ProductMapper.mapToProduct(productRequest);

        try {
            // Step 3: Save the entity and log success
            Product savedProduct = productRepository.save(product);
            log.info("Product with ID {} has been successfully created!", savedProduct.getId());
            return savedProduct;
        } catch (Exception e) {
            // Step 4: Handle potential errors
            log.error("Failed to save product: {}", e.getMessage());
            throw new RuntimeException("Could not save product", e);
        }
    }

    public List<ProductResponse> getAllProducts() {

        List<Product> products = productRepository.findAll();
        return products.stream()
                .map(ProductMapper::mapToProductResponse)
                .toList();
    }

    public ProductRequest getProductById(String id) {
        // Input validation
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty");
        }

        // Log the operation
        log.info("Fetching product with ID: {}", id);

        return productRepository.findById(id)
                .map(ProductMapper::mapToProductRequest)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
    }

//    public ProductResponse getProductById(String id) {
//        // Input validation
//        if (id == null || id.trim().isEmpty()) {
//            throw new IllegalArgumentException("Product ID cannot be null or empty");
//        }
//
//        // Log the operation
//        log.info("Fetching product with ID: {}", id);
//
//        return productRepository.findById(id)
//                .map(this::mapToProductResponse)
//                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
//    }

//    private ProductResponse mapToProductResponse(Product product) {
//        return ProductResponse.builder()
//                .id(product.getId())
//                .name(product.getName())
//                .description(product.getDescription())
//                .price(product.getPrice())
//                .build();
//    }

}
