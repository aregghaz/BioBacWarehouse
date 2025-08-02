package com.biobac.warehouse.service;

import com.biobac.warehouse.dto.RecipeItemDto;

import java.util.List;

public interface RecipeItemService {
    List<RecipeItemDto> getAllRecipeItems();
    RecipeItemDto getRecipeItemById(Long id);
    List<RecipeItemDto> getRecipeItemsByProductId(Long productId);
    List<RecipeItemDto> getRecipeItemsByIngredientId(Long ingredientId);
    RecipeItemDto createRecipeItem(RecipeItemDto recipeItemDto, Long productId);
    RecipeItemDto updateRecipeItem(Long id, RecipeItemDto recipeItemDto);
    void deleteRecipeItem(Long id);
}