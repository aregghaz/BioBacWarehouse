package com.biobac.warehouse.service;

import com.biobac.warehouse.dto.IngredientHistoryDto;
import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.IngredientHistoryResponse;
import com.biobac.warehouse.response.IngredientHistorySingleResponse;
import org.springframework.data.util.Pair;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface IngredientHistoryService {

    IngredientHistorySingleResponse recordQuantityChange(IngredientHistoryDto dto);

    Pair<List<IngredientHistorySingleResponse>, PaginationMetadata> getHistoryForIngredient(Long ingredientId, Map<String, FilterCriteria> filters, Integer page, Integer size, String sortBy, String sortDir);

    Pair<List<IngredientHistoryResponse>, PaginationMetadata> getAll(Map<String, FilterCriteria> filters, Integer page, Integer size, String sortBy, String sortDir);

    Pair<List<IngredientHistorySingleResponse>, PaginationMetadata> getHistory(Map<String, FilterCriteria> filters, Integer page, Integer size, String sortBy, String sortDir);

    Double getTotalForIngredient(Long ingredientId);

    Double getInitialForIngredient(Long ingredientId, LocalDateTime start);

    Double getEventualForIngredient(Long ingredientId, LocalDateTime end);

    Double getEventualForIngredient(Long ingredientId, Long warehouseId, LocalDateTime end);

    Double getSumOfIncreasedCount(Long id, LocalDateTime start, LocalDateTime end);

    Double getSumOfDecreasedCount(Long id, LocalDateTime start, LocalDateTime end);
}