package com.arseniolourenco.product_service.repository;

import com.arseniolourenco.product_service.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepositoy extends MongoRepository<Product, String> {
}
