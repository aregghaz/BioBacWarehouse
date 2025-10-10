package com.biobac.warehouse.service;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.entity.Ingredient;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.IngredientHistoryResponse;
import org.springframework.data.util.Pair;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface IngredientHistoryService {

    IngredientHistoryResponse recordQuantityChange(Ingredient ingredient, Double quantityResult,
                                                   Double quantityChange, String notes, BigDecimal lastPrice, Long lastCompanyId);


    Pair<List<IngredientHistoryResponse>, PaginationMetadata> getHistoryForIngredient(Long ingredientId, Map<String, FilterCriteria> filters, Integer page, Integer size, String sortBy, String sortDir);


    Pair<List<IngredientHistoryResponse>, PaginationMetadata> getHistory(Map<String, FilterCriteria> filters, Integer page, Integer size, String sortBy, String sortDir);

    Double getTotalForIngredient(Long ingredientId, Map<String, FilterCriteria> filters);
}