package com.biobac.warehouse.mapper;

import com.biobac.warehouse.dto.RecipeItemDto;
import com.biobac.warehouse.entity.RecipeItem;
import com.biobac.warehouse.response.RecipeItemTableResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface RecipeItemMapper {

    @Mappings({
            @Mapping(target = "id", source = "id"),
            @Mapping(target = "ingredientId", source = "ingredient.id"),
            @Mapping(target = "ingredientName", source = "ingredient.name"),
            @Mapping(target = "ingredientUnit", source = "ingredient.unit"),
            @Mapping(target = "quantity", source = "quantity"),
            @Mapping(target = "notes", source = "notes"),
            @Mapping(target = "productId", source = "product.id")
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

    @Mappings({
            @Mapping(target = "id", source = "id"),
            @Mapping(target = "ingredientName", source = "ingredient.name"),
            @Mapping(target = "ingredientUnit", source = "ingredient.unit"),
            @Mapping(target = "quantity", source = "quantity"),
            @Mapping(target = "notes", source = "notes"),
            @Mapping(target = "productName", source = "product.name")
    })
    RecipeItemTableResponse toTableResponse(RecipeItem recipeItem);
}