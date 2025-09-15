package com.biobac.warehouse.mapper;

import com.biobac.warehouse.entity.InventoryItem;
import com.biobac.warehouse.response.InventoryItemResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InventoryItemMapper {
    @Mapping(target = "warehouseName", source = "warehouse.name")
    @Mapping(target = "warehouseId", source = "warehouse.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "ingredientName", source = "ingredient.name")
    @Mapping(target = "companyName", ignore = true)
    InventoryItemResponse toSingleResponse(InventoryItem item);
}
