package com.biobac.warehouse.service;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.request.InventoryIngredientCreateRequest;
import com.biobac.warehouse.request.InventoryProductCreateRequest;
import com.biobac.warehouse.response.InventoryItemResponse;
import org.springframework.data.util.Pair;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface InventoryItemService {
    @Transactional
    InventoryItemResponse createForProduct(InventoryProductCreateRequest request);

    @Transactional
    InventoryItemResponse createForIngredient(InventoryIngredientCreateRequest request);

    @Transactional(readOnly = true)
    Pair<List<InventoryItemResponse>, PaginationMetadata> getByProductId(Long productId, Map<String, FilterCriteria> filters,
                                                                         Integer page,
                                                                         Integer size,
                                                                         String sortBy,
                                                                         String sortDir);

    @Transactional(readOnly = true)
    Pair<List<InventoryItemResponse>, PaginationMetadata> getByIngredientId(Long ingredientId, Map<String, FilterCriteria> filters,
                                                                            Integer page,
                                                                            Integer size,
                                                                            String sortBy,
                                                                            String sortDir);

    @Transactional(readOnly = true)
    Pair<List<InventoryItemResponse>, PaginationMetadata> getAll(Map<String, FilterCriteria> filters,
                                                                 Integer page,
                                                                 Integer size,
                                                                 String sortBy,
                                                                 String sortDir);

    @Transactional(readOnly = true)
    Map<Long, List<InventoryItemResponse>> getAllByIngredientIds(List<Long> ids);

    @Transactional(readOnly = true)
    Map<Long, List<InventoryItemResponse>> getAllByProductIds(List<Long> ids);

    @Transactional(readOnly = true)
    Pair<List<InventoryItemResponse>, PaginationMetadata> getByWarehouseId(Long warehouseId, Map<String, FilterCriteria> filters, Integer page, Integer size, String sortBy, String sortDir);
}
