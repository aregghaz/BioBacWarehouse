package com.biobac.warehouse.mapper;

import com.biobac.warehouse.dto.WarehouseDto;
import com.biobac.warehouse.entity.Warehouse;
import com.biobac.warehouse.response.WarehouseResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WarehouseMapper {
    WarehouseDto toDto(Warehouse warehouse);

    Warehouse toEntity(WarehouseDto dto);

    WarehouseResponse toResponse(Warehouse warehouse);
}
