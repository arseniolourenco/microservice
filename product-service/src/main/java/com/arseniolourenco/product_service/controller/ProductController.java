package com.arseniolourenco.product_service.controller;

import java.util.List;

import com.arseniolourenco.product_service.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.arseniolourenco.product_service.dto.ProductRequest;
import com.arseniolourenco.product_service.dto.ProductResponse;
import com.arseniolourenco.product_service.service.ProductService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/product")
public class ProductController {

    @Autowired
    private final ProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Product createProduct(@RequestBody ProductRequest productRequest) {
        return productService.createProduct(productRequest);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ProductResponse> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<ProductRequest> getProductById(@PathVariable String id) {
        ProductRequest productRequest = productService.getProductById(id);
        return ResponseEntity.ok(productRequest);
    }

}
