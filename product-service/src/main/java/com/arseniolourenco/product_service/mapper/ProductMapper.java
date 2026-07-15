package com.arseniolourenco.product_service.mapper;

import com.arseniolourenco.product_service.dto.ProductRequest;
import com.arseniolourenco.product_service.dto.ProductResponse;
import com.arseniolourenco.product_service.model.Product;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    // Request DTO → Entity (used in create/update operations)
    @org.mapstruct.Mapping(target = "id", ignore = true)
    Product mapToProduct(ProductRequest productRequest);

    // Entity → Response DTO (used in GET operations)
    ProductResponse mapToProductResponse(Product product);
}
