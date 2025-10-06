package com.biobac.warehouse.mapper;

import com.biobac.warehouse.entity.ManufactureProduct;
import com.biobac.warehouse.response.ManufactureProductResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ManufactureProductMapper {
    ManufactureProductResponse toSingleResponse(ManufactureProduct entity);
}
