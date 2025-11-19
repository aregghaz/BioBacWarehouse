package com.biobac.warehouse.mapper;

import com.biobac.warehouse.entity.WarehouseType;
import com.biobac.warehouse.response.WarehouseTypeResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WarehouseTypeMapper {
    WarehouseTypeResponse toResponse(WarehouseType companyType);
}
