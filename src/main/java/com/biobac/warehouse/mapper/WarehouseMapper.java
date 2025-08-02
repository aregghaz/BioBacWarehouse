package com.biobac.warehouse.mapper;

import com.biobac.warehouse.dto.WarehouseDto;
import com.biobac.warehouse.entity.Warehouse;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface WarehouseMapper {
    WarehouseDto toDto(Warehouse warehouse);
    Warehouse toEntity(WarehouseDto dto);
}
