package com.arseniolourenco.product_service.mapper;

import com.arseniolourenco.product_service.dto.ProductRequest;
import com.arseniolourenco.product_service.dto.ProductResponse;
import com.arseniolourenco.product_service.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    // Request DTO → Entity (used in create/update operations)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Product mapToProduct(ProductRequest productRequest);

    // Entity → Response DTO (used in GET operations)
    ProductResponse mapToProductResponse(Product product);
}
