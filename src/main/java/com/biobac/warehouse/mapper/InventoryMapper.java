package com.biobac.warehouse.mapper;
import com.biobac.warehouse.dto.InventoryItemDto;
import com.biobac.warehouse.entity.InventoryItem;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface InventoryMapper {
    InventoryItemDto toDto(InventoryItem entity);
    InventoryItem toEntity(InventoryItemDto dto);
}
