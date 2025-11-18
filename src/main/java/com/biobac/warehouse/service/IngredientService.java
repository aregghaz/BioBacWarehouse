package com.biobac.warehouse.service;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.entity.Ingredient;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.request.IngredientCreateRequest;
import com.biobac.warehouse.request.IngredientUpdateRequest;
import com.biobac.warehouse.response.IngredientResponse;
import org.springframework.data.util.Pair;

import java.util.List;
import java.util.Map;

public interface IngredientService extends UnitTypeCalculator{
    IngredientResponse create(IngredientCreateRequest ingredient);

    IngredientResponse getById(Long id);

    Ingredient getIngredientById(Long id);

    List<IngredientResponse> getAll();

    Pair<List<IngredientResponse>, PaginationMetadata> getPagination(Map<String, FilterCriteria> filters, Integer page, Integer size, String sortBy, String sortDir);

    IngredientResponse update(Long id, IngredientUpdateRequest request);

    void delete(Long id);
}
