package com.biobac.warehouse.service;

import com.biobac.warehouse.dto.IngredientHistoryDto;
import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.entity.Ingredient;
import com.biobac.warehouse.request.FilterCriteria;
import org.springframework.data.util.Pair;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface IngredientHistoryService {

    IngredientHistoryDto recordQuantityChange(Ingredient ingredient, Double quantityBefore,
                                              Double quantityAfter, String action, String notes, BigDecimal lastPrice, Long lastCompanyId);


    Pair<List<IngredientHistoryDto>, PaginationMetadata> getHistoryForIngredient(Long ingredientId, Map<String, FilterCriteria> filters, Integer page, Integer size, String sortBy, String sortDir);


    Pair<List<IngredientHistoryDto>, PaginationMetadata> getHistory(Map<String, FilterCriteria> filters, Integer page, Integer size, String sortBy, String sortDir);
}