package com.biobac.warehouse.mapper;

import com.biobac.warehouse.dto.ProductHistoryDto;
import com.biobac.warehouse.entity.ProductHistory;
import com.biobac.warehouse.response.ProductHistoryResponse;
import com.biobac.warehouse.response.ProductHistorySingleResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductHistoryMapper {

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "product.unit.name", target = "unitName")
    @Mapping(source = "product.productGroup.name", target = "productGroupName")
    @Mapping(source = "warehouse.id", target = "warehouseId")
    @Mapping(source = "warehouse.name", target = "warehouseName")
    @Mapping(source = "action.id", target = "actionId")
    @Mapping(source = "action.name", target = "actionName")
    ProductHistorySingleResponse toSingleResponse(ProductHistory entity);

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "product.unit.name", target = "unitName")
    @Mapping(source = "product.productGroup.name", target = "productGroupName")
    ProductHistoryResponse toResponse(ProductHistory entity);

    ProductHistoryDto toDto(ProductHistory entity);
    List<ProductHistoryDto> toDtoList(List<ProductHistory> entities);

    @Mapping(target = "product", ignore = true)
    ProductHistory toEntity(ProductHistoryDto dto);
}