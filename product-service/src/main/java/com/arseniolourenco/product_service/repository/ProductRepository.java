package com.arseniolourenco.product_service.repository;

import com.arseniolourenco.product_service.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {

    Optional<Product> findBySkuCode(String skuCode);

    boolean existsBySkuCode(String skuCode);

    List<Product> findByNameContainingIgnoreCase(String name);
}