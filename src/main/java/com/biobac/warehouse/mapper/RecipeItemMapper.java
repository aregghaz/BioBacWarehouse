package com.biobac.warehouse.mapper;

import com.biobac.warehouse.entity.Ingredient;
import com.biobac.warehouse.entity.RecipeComponent;
import com.biobac.warehouse.entity.RecipeItem;
import com.biobac.warehouse.request.RecipeComponentRequest;
import com.biobac.warehouse.request.RecipeItemCreateRequest;
import com.biobac.warehouse.response.RecipeItemResponse;
import com.biobac.warehouse.response.RecipeItemTableResponse;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RecipeItemMapper {

    // ------------------ CREATE REQUEST → ENTITY ------------------ //

    @Mapping(target = "components", source = "components")
    RecipeItem toEntity(RecipeItemCreateRequest request);

    @Mapping(target = "ingredient", source = "ingredient")
    @Mapping(target = "recipeItem", ignore = true) // will set in service
    @Mapping(target = "unitId", ignore = true) // handled/validated in service layer
    RecipeComponent toEntity(RecipeComponentRequest request, Ingredient ingredient);

    // ------------------ ENTITY → DTO ------------------ //

    @Mapping(target = "components", source = "components")
    @Mapping(target = "notes", source = "notes")
    RecipeItemResponse toDto(RecipeItem entity);

    @Mapping(target = "ingredientId", source = "ingredient.id")
    @Mapping(target = "ingredientName", source = "ingredient.name")
    RecipeItemResponse.RecipeComponentDto toDto(RecipeComponent entity);

    List<RecipeComponent> toEntityComponents(List<RecipeComponentRequest> requests, @Context RecipeItem recipeItem, @Context List<Ingredient> ingredients);

    List<RecipeItemResponse.RecipeComponentDto> toDtoComponents(List<RecipeComponent> components);

    RecipeItemTableResponse toTableResponse(RecipeItem recipeItem);
}