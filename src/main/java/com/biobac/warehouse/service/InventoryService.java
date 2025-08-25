
package com.biobac.warehouse.service;

import com.biobac.warehouse.dto.InventoryItemDto;
import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.request.InventoryItemCreateRequest;
import com.biobac.warehouse.request.InventoryItemUpdateRequest;
import com.biobac.warehouse.response.InventoryItemTableResponse;
import org.springframework.data.util.Pair;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface InventoryService {
    @Transactional(readOnly = true)
    List<InventoryItemDto> getAll();

    @Transactional(readOnly = true)
    Pair<List<InventoryItemTableResponse>, PaginationMetadata> getPagination(Map<String, FilterCriteria> filters,
                                                                             Integer page,
                                                                             Integer size,
                                                                             String sortBy,
                                                                             String sortDi);

    @Transactional(readOnly = true)
    InventoryItemDto getById(Long id);

    @Transactional
    InventoryItemDto create(InventoryItemDto dto);

    @Transactional
    InventoryItemDto create(InventoryItemCreateRequest dto);

    @Transactional
    InventoryItemDto update(Long id, InventoryItemDto dto);

    @Transactional
    InventoryItemDto update(Long id, InventoryItemUpdateRequest dto);

    @Transactional
    void delete(Long id);

    // Methods for finding by product and ingredient
    @Transactional(readOnly = true)
    List<InventoryItemDto> findByProductId(Long productId);

    @Transactional(readOnly = true)
    List<InventoryItemDto> findByIngredientId(Long ingredientId);

    // Methods for finding by warehouse and group
    @Transactional(readOnly = true)
    List<InventoryItemDto> findByWarehouseId(Long warehouseId);

    @Transactional(readOnly = true)
    List<InventoryItemDto> findByGroupId(Long groupId);
}
