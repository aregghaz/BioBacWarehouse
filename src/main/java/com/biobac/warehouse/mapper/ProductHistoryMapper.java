package com.biobac.warehouse.mapper;

import com.biobac.warehouse.dto.ProductHistoryDto;
import com.biobac.warehouse.entity.ProductHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductHistoryMapper {

    ProductHistoryMapper INSTANCE = Mappers.getMapper(ProductHistoryMapper.class);

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    ProductHistoryDto toDto(ProductHistory entity);

    List<ProductHistoryDto> toDtoList(List<ProductHistory> entities);

    @Mapping(target = "product", ignore = true)
    ProductHistory toEntity(ProductHistoryDto dto);
}