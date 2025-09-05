package com.biobac.warehouse.mapper;

import com.biobac.warehouse.dto.WarehouseGroupDto;
import com.biobac.warehouse.entity.WarehouseGroup;
import com.biobac.warehouse.response.WarehouseGroupResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WarehouseGroupMapper {
    WarehouseGroupResponse toDto(WarehouseGroup entity);
    WarehouseGroupResponse toResponse(WarehouseGroup entity);
    WarehouseGroup toEntity(WarehouseGroupDto dto);
    WarehouseGroupResponse toTableResponse(WarehouseGroup entity);
}
