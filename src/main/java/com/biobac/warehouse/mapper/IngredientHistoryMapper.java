package com.biobac.warehouse.mapper;

import com.biobac.warehouse.entity.IngredientHistory;
import com.biobac.warehouse.response.IngredientHistoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IngredientHistoryMapper {

    @Mapping(source = "ingredient.id", target = "ingredientId")
    @Mapping(source = "ingredient.name", target = "ingredientName")
    IngredientHistoryResponse toResponse(IngredientHistory entity);
}