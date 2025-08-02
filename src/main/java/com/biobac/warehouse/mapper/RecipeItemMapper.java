package com.biobac.warehouse.mapper;

import com.biobac.warehouse.dto.RecipeItemDto;
import com.biobac.warehouse.entity.Ingredient;
import com.biobac.warehouse.entity.RecipeItem;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface RecipeItemMapper {

    @Mappings({
            @Mapping(target = "id", source = "id"),
            @Mapping(target = "ingredientId", source = "ingredient.id"),
            @Mapping(target = "ingredientName", source = "ingredient.name"),
            @Mapping(target = "ingredientUnit", source = "ingredient.unit"),
            @Mapping(target = "quantity", source = "quantity"),
            @Mapping(target = "notes", source = "notes")
    })
    RecipeItemDto toDto(RecipeItem recipeItem);

    @Mappings({
            @Mapping(target = "id", source = "id"),
            @Mapping(target = "ingredient", ignore = true), // Set in service
            @Mapping(target = "product", ignore = true),    // Set in service
            @Mapping(target = "quantity", source = "quantity"),
            @Mapping(target = "notes", source = "notes")
    })
    RecipeItem toEntity(RecipeItemDto dto);
}