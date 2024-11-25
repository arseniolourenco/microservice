package com.arseniolourenco.product_service.service;

import com.arseniolourenco.product_service.dto.ProductRequest;
import com.arseniolourenco.product_service.dto.ProductResponse;
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

//    public void createProduct(ProductRequest productRequest) {
//
//        // Map the ProductRequest DTO to a Product entity
//        Product product = mapToProduct(productRequest);
//
//        // Save the product to the repository
//        productRepository.save(product);
//
//        // Log success message with the saved product ID
//        log.info("Product with ID {} has been successfully created!", product.getId());
//    }
//
//    private Product mapToProduct(ProductRequest productRequest) {
//        return Product.builder()
//                .name(productRequest.getName())
//                .description(productRequest.getDescription())
//                .price(productRequest.getPrice())
//                .build();
//    }

    public void createProduct(ProductRequest productRequest) {
        Product product = Product.builder()
                .name(productRequest.getName())
                .description(productRequest.getDescription())
                .price(productRequest.getPrice())
                .build();

        log.info("Product with ID {} has been successfully created!", product.getId());

        productRepository.save(product);
    }

    public List<ProductResponse> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream()
                .map(this::mapToProductResponse)
                .toList();
    }


    public ProductResponse getProductById(String id) {
        return productRepository.findById(id)
                .map(this::mapToProductResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
    }

    private ProductResponse mapToProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .build();
    }

//    public Product saveProduct(ProductRequest productRequest) {
//
//        Product product = Product.builder()
//                .name(productRequest.getName())
//                .description(productRequest.getDescription())
//                .price(productRequest.getPrice())
//                .build();
//
//        productRepository.save(product);
//        log.info("Product {} is saved!", product.getId());
//        return product;
//    }
}
