package com.biobac.warehouse.service;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.dto.RecipeItemDto;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.RecipeItemTableResponse;
import com.biobac.warehouse.response.WarehouseTableResponse;
import org.springframework.data.util.Pair;

import java.util.List;
import java.util.Map;

public interface RecipeItemService {
    Pair<List<RecipeItemTableResponse>, PaginationMetadata> getAllRecipeItems(Map<String, FilterCriteria> filters,
                                                                              Integer page,
                                                                              Integer size,
                                                                              String sortBy,
                                                                              String sortDir);
    RecipeItemDto getRecipeItemById(Long id);
    List<RecipeItemDto> getRecipeItemsByProductId(Long productId);
    List<RecipeItemDto> getRecipeItemsByIngredientId(Long ingredientId);
    RecipeItemDto createRecipeItem(RecipeItemDto recipeItemDto, Long productId);
    RecipeItemDto updateRecipeItem(Long id, RecipeItemDto recipeItemDto);
    void deleteRecipeItem(Long id);
}