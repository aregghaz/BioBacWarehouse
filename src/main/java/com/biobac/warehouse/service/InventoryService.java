
package com.biobac.warehouse.service;

import com.biobac.warehouse.dto.InventoryItemDto;

import java.util.List;

public interface InventoryService {
    List<InventoryItemDto> getAll();
    InventoryItemDto getById(Long id);
    InventoryItemDto create(InventoryItemDto dto);
    InventoryItemDto update(Long id, InventoryItemDto dto);
    void delete(Long id);
    
    // Methods for finding by product and ingredient
    List<InventoryItemDto> findByProductId(Long productId);
    List<InventoryItemDto> findByIngredientId(Long ingredientId);
}
