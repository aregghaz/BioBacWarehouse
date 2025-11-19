package com.biobac.warehouse.mapper;

import com.biobac.warehouse.dto.UnitTypeDto;
import com.biobac.warehouse.entity.UnitType;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UnitTypeMapper {
    UnitTypeDto toDto(UnitType unitType);
}