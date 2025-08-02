package com.biobac.warehouse.mapper;

import com.biobac.warehouse.dto.ProductDto;
import com.biobac.warehouse.entity.Product;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductDto toDto(Product product);
    Product toEntity(ProductDto dto);
}
