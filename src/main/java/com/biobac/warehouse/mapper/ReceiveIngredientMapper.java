package com.biobac.warehouse.mapper;

import com.biobac.warehouse.entity.ReceiveIngredient;
import com.biobac.warehouse.response.ReceiveIngredientResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReceiveIngredientMapper {
    @Mapping(source = "warehouse.name", target = "warehouseName")
    @Mapping(source = "warehouse.id", target = "warehouseId")
    @Mapping(source = "ingredient.name", target = "ingredientName")
    @Mapping(source = "ingredient.id", target = "ingredientId")
    @Mapping(source = "ingredient.unit.name", target = "unitName")
    ReceiveIngredientResponse toSingleResponse(ReceiveIngredient item);
}
