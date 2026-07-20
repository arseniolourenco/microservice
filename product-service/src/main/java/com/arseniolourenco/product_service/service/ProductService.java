package com.arseniolourenco.product_service.service;

import com.arseniolourenco.product_service.dto.ProductRequest;
import com.arseniolourenco.product_service.dto.ProductResponse;
import com.arseniolourenco.product_service.exception.ProductNotFoundException;
import com.arseniolourenco.product_service.mapper.ProductMapper;
import com.arseniolourenco.product_service.model.Product;
import com.arseniolourenco.product_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper; // injected

    /**
     * Create a new product.
     */
    @CacheEvict(value = {"products", "product"}, allEntries = true)
    public ProductResponse createProduct(ProductRequest productRequest) {

        // Convert ProductRequest to Product using the mapper
        Product product = productMapper.mapToProduct(productRequest);
        if (product == null) {
            throw new IllegalArgumentException("Mapped product cannot be null");
        }

        // Save to database
        Product savedProduct = productRepository.save(product);

        log.info("Product with ID {} created!", savedProduct.getId());

        // Convert saved Product entity to ProductResponse DTO using the mapper
        return productMapper.mapToProductResponse(savedProduct);
    }

    /**
     * Retrieve all products.
     */
    @Cacheable(value = "products", key = "'all'")
    public List<ProductResponse> getAllProducts() {

        List<Product> products = productRepository.findAll();

        log.info("Fetched {} products", products.size());

        return products.stream()
                .map(productMapper::mapToProductResponse)
                .toList();
    }

    /**
     * Retrieve a product by ID.
     */
    @Cacheable(value = "product", key = "#productId")
    public ProductResponse getProductById(String productId) {

        Product existProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product With ID: " + productId + " Not Found"));

        return productMapper.mapToProductResponse(existProduct);
    }

    /**
     * Update an existing product.
     */
    @CacheEvict(value = {"products", "product"}, allEntries = true)
    public ProductResponse updateProduct(String productId, ProductRequest productRequest) {

        Product existProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product With ID: " + productId + " Not Found"));

        existProduct.setName(productRequest.name());
        existProduct.setSkuCode(productRequest.skuCode());
        existProduct.setDescription(productRequest.description());
        existProduct.setPrice(productRequest.price());
        Product updatedProduct = productRepository.save(existProduct);

        log.info("Product with ID {} updated!", productId);
        return productMapper.mapToProductResponse(updatedProduct);
    }

    /**
     * Delete a product by ID.
     */
    @CacheEvict(value = {"products", "product"}, allEntries = true)
    public void deleteProduct(String productId) {
        Product existProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product With ID: " + productId + " Not Found"));

        productRepository.delete(existProduct);

        log.info("Product with ID {} deleted!", productId);
    }
}
