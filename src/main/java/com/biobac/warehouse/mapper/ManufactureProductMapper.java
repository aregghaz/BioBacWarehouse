package com.biobac.warehouse.mapper;

import com.biobac.warehouse.entity.ManufactureProduct;
import com.biobac.warehouse.response.ManufactureProductResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ManufactureProductMapper {
    @Mapping(source = "warehouse.name", target = "warehouseName")
    @Mapping(source = "warehouse.id", target = "warehouseId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "product.id", target = "productId")
    ManufactureProductResponse toSingleResponse(ManufactureProduct entity);
}
