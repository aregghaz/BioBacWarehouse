package com.biobac.warehouse.mapper;

import com.biobac.warehouse.entity.InventoryItem;
import com.biobac.warehouse.response.InventoryItemResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InventoryItemMapper {
    @Mapping(target = "warehouseName", source = "warehouse.name")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "ingredientName", source = "ingredient.name")
    InventoryItemResponse toSingleResponse(InventoryItem item);
}
