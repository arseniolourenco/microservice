package com.arseniolourenco.product_service.mapper;

import com.arseniolourenco.product_service.dto.ProductRequest;
import com.arseniolourenco.product_service.dto.ProductResponse;
import com.arseniolourenco.product_service.model.Product;

public class ProductMapper {

    // Map from ProductRequest to Product
    public static Product mapToProduct(ProductRequest productRequest) {
        return Product.builder()
                .name(productRequest.getName())
                .description(productRequest.getDescription())
                .price(productRequest.getPrice())
                .build();
    }

    // Optional: Map from Product to ProductRequest if needed
    public static ProductRequest mapToProductRequest(Product product) {
        return ProductRequest.builder()
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .build();
    }

    // Optional: Map from Product to ProductResponse if needed
    public static ProductResponse mapToProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .build();
    }
}
