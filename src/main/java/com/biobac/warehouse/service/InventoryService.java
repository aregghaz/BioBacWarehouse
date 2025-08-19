
package com.biobac.warehouse.service;

import com.biobac.warehouse.dto.InventoryItemDto;
import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.InventoryItemTableResponse;
import org.springframework.data.util.Pair;

import java.util.List;
import java.util.Map;

public interface InventoryService {
    Pair<List<InventoryItemTableResponse>, PaginationMetadata> getAll(Map<String, FilterCriteria> filters,
                                                                      Integer page,
                                                                      Integer size,
                                                                      String sortBy,
                                                                      String sortDi);
    InventoryItemDto getById(Long id);
    InventoryItemDto create(InventoryItemDto dto);
    InventoryItemDto update(Long id, InventoryItemDto dto);
    void delete(Long id);
    
    // Methods for finding by product and ingredient
    List<InventoryItemDto> findByProductId(Long productId);
    List<InventoryItemDto> findByIngredientId(Long ingredientId);
    
    // Methods for finding by warehouse and group
    List<InventoryItemDto> findByWarehouseId(Long warehouseId);
    List<InventoryItemDto> findByGroupId(Long groupId);
}
