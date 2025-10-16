package com.biobac.warehouse.mapper;

import com.biobac.warehouse.entity.IngredientHistory;
import com.biobac.warehouse.response.IngredientHistoryResponse;
import com.biobac.warehouse.response.IngredientHistorySingleResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IngredientHistoryMapper {

    @Mapping(source = "ingredient.id", target = "ingredientId")
    @Mapping(source = "ingredient.name", target = "ingredientName")
    @Mapping(source = "ingredient.unit.name", target = "unitName")
    IngredientHistorySingleResponse toSingleResponse(IngredientHistory entity);

    @Mapping(source = "ingredient.id", target = "ingredientId")
    @Mapping(source = "ingredient.name", target = "ingredientName")
    @Mapping(source = "ingredient.unit.name", target = "unitName")
    IngredientHistoryResponse toResponse(IngredientHistory entity);
}