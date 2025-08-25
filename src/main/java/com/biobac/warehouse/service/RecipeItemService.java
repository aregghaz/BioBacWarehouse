package com.biobac.warehouse.service;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.dto.RecipeItemDto;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.request.IngredientCreateRequest;
import com.biobac.warehouse.request.RecipeItemCreateRequest;
import com.biobac.warehouse.response.RecipeItemResponse;
import com.biobac.warehouse.response.RecipeItemTableResponse;
import org.springframework.data.util.Pair;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface RecipeItemService {
    @Transactional(readOnly = true)
    Pair<List<RecipeItemTableResponse>, PaginationMetadata> getPagination(Map<String, FilterCriteria> filters,
                                                                          Integer page,
                                                                          Integer size,
                                                                          String sortBy,
                                                                          String sortDir);

    @Transactional(readOnly = true)
    List<RecipeItemDto> getAll();

    @Transactional(readOnly = true)
    RecipeItemDto getRecipeItemById(Long id);

    @Transactional(readOnly = true)
    List<RecipeItemDto> getRecipeItemsByProductId(Long productId);

    @Transactional(readOnly = true)
    List<RecipeItemDto> getRecipeItemsByIngredientId(Long ingredientId);

    @Transactional
    RecipeItemResponse createRecipeItem(RecipeItemCreateRequest recipeItemCreateRequest);

    @Transactional
    RecipeItemDto updateRecipeItem(Long id, RecipeItemDto recipeItemDto);

    @Transactional
    void deleteRecipeItem(Long id);
}