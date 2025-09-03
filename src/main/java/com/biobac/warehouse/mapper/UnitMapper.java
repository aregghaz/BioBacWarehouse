package com.biobac.warehouse.mapper;

import com.biobac.warehouse.dto.UnitDto;
import com.biobac.warehouse.entity.Unit;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {UnitTypeMapper.class})
public interface UnitMapper {
    UnitDto toDto(Unit unit);
}