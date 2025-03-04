package com.arseniolourenco.product_service.service;

import com.arseniolourenco.product_service.dto.ProductRequest;
import com.arseniolourenco.product_service.dto.ProductResponse;
import com.arseniolourenco.product_service.mapper.ProductMapper;
import com.arseniolourenco.product_service.model.Product;
import com.arseniolourenco.product_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;


@RequiredArgsConstructor
@Slf4j
@Service
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

    /**
     * Create a new product.
     */
//    public Product createProduct(ProductRequest productRequest) {
//        // Step 1: Validate the request
//        if (productRequest.getName() == null || productRequest.getName().isEmpty()) {
//            throw new IllegalArgumentException("Product name cannot be null or empty");
//        }
//
//        // Step 2: Map the DTO to an entity
//        Product product = ProductMapper.mapToProduct(productRequest);
//
//        try {
//            // Step 3: Save the entity and log success
//            Product savedProduct = productRepository.save(product);
//            log.info("Product with ID {} has been successfully created!", savedProduct.getId());
//            return savedProduct;
//        } catch (Exception e) {
//            // Step 4: Handle potential errors
//            log.error("Failed to save product: {}", e.getMessage());
//            throw new RuntimeException("Could not save product", e);
//        }
//    }
//    public ProductRequest createProduct(Product product) {
//        validateProductRequest(product);
//
//        Product product = ProductMapper.mapToProductRequest(product);
//
//        try {
//            Product savedProduct = productRepository.save(product);
//            log.info("Product with ID {} has been successfully created!", savedProduct.getId());
//            return savedProduct;
//        } catch (Exception e) {
//            log.error("Failed to save product: {}", e.getMessage());
//            throw new RuntimeException("Could not save product", e);
//        }
//    }
//    public Product createProduct(ProductRequest product) {
//
////        validateProduct(product);
//
//        try {
//            Product savedProduct = productRepository.save(product);
//
//            log.info("Product with ID {} has been successfully created!", savedProduct.getId());
//
//            // Map saved Product entity to ProductResponse DTO
//            return ProductMapper.mapToProduct(savedProduct);
//
//        } catch (Exception e) {
//            log.error("Failed to save product: {}", e.getMessage());
//            throw new RuntimeException("Could not save product", e);
//        }
//    }
    public ProductResponse createProduct(ProductRequest productRequest) {

        // Convert ProductRequest to Product entity
        Product product = Product.builder()
                .name(productRequest.getName())
                .description(productRequest.getDescription())
                .price(productRequest.getPrice())
                .build();

        try {
            Product savedProduct = productRepository.save(product);

            log.info("Product with ID {} has been successfully created!", savedProduct.getId());

            // Convert saved Product entity to ProductResponse DTO
            return ProductMapper.mapToProductResponse(savedProduct);

        } catch (Exception e) {
            log.error("Failed to save product: {}", e.getMessage());
            throw new RuntimeException("Could not save product", e);
        }
    }

    /**
     * Retrieve all products.
     */
    public List<ProductResponse> getAllProducts() {

        List<Product> products = productRepository.findAll();

        log.info("List of Products: {} ", products);

        return products.stream()
                .map(ProductMapper::mapToProductResponse)
                .toList();
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
//                .map(ProductMapper::mapToProductResponse)
//                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
//    }

    /**
     * Retrieve a product by ID.
     */
//    public ProductResponse getProductById(String id) {
//        validateProductId(id);
//
//        return productRepository.findById(id)
//                .map(ProductMapper::mapToProductResponse)
//                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + id));
//    }
    public ProductResponse getProductById(String productId) {
        validateProductId(productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));

        log.info("Product with ID {} has been retrieved", product.getId());

        return ProductMapper.mapToProductResponse(product);
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

    /**
     * Update an existing product.
     */
    public ProductResponse updateProduct(String productId, ProductRequest productRequest) {
        validateProductId(productId);

        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId));

        // Update fields
        existingProduct.setName(productRequest.getName());
        existingProduct.setDescription(productRequest.getDescription());
        existingProduct.setPrice(productRequest.getPrice());

        Product updatedProduct = productRepository.save(existingProduct);
        log.info("Product with ID {} has been successfully updated!", updatedProduct.getId());

        return ProductMapper.mapToProductResponse(updatedProduct);
    }

    /**
     * Delete a product by ID.
     */
    public void deleteProduct(String productId) {
        validateProductId(productId);

        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));

        productRepository.deleteById(existingProduct.getId());
        log.info("Product with ID {} has been successfully deleted!", productId);
    }

    /**
     * Validate the product request.
     */
    private void validateProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product request cannot be null");
        }
        if (product.getName() == null || product.getName().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be null or empty");
        }
        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Product price must be greater than zero");
        }
    }

    /**
     * Validate the product ID.
     */
    private void validateProductId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty");
        }
    }

}
