package com.biobac.warehouse.service;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.request.RecipeItemCreateRequest;
import com.biobac.warehouse.response.RecipeItemResponse;
import com.biobac.warehouse.response.RecipeItemTableResponse;
import org.springframework.data.util.Pair;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface RecipeItemService {
    Pair<List<RecipeItemTableResponse>, PaginationMetadata> getPagination(Map<String, FilterCriteria> filters,
                                                                          Integer page,
                                                                          Integer size,
                                                                          String sortBy,
                                                                          String sortDir);

    List<RecipeItemResponse> getAll();

    RecipeItemResponse getRecipeItemById(Long id);

    RecipeItemResponse createRecipeItem(RecipeItemCreateRequest recipeItemCreateRequest);

    RecipeItemResponse updateRecipeItem(Long id, RecipeItemCreateRequest recipeItemCreateRequest);

    void deleteRecipeItem(Long id);
}