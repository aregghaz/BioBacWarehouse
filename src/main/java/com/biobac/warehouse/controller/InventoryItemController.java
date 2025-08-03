package com.biobac.warehouse.controller;

import com.biobac.warehouse.dto.InventoryItemDto;
import com.biobac.warehouse.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryItemController {
    private final InventoryService service;

    @GetMapping
    public List<InventoryItemDto> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public InventoryItemDto getById(@PathVariable Long id) {
        return service.getById(id);
    }
    
    @GetMapping("/product/{productId}")
    public List<InventoryItemDto> findByProductId(@PathVariable Long productId) {
        return service.findByProductId(productId);
    }
    
    @GetMapping("/ingredient/{ingredientId}")
    public List<InventoryItemDto> findByIngredientId(@PathVariable Long ingredientId) {
        return service.findByIngredientId(ingredientId);
    }
    
    @GetMapping("/warehouse/{warehouseId}")
    public List<InventoryItemDto> findByWarehouseId(@PathVariable Long warehouseId) {
        return service.findByWarehouseId(warehouseId);
    }
    
    @GetMapping("/group/{groupId}")
    public List<InventoryItemDto> findByGroupId(@PathVariable Long groupId) {
        return service.findByGroupId(groupId);
    }

    @PostMapping
    public InventoryItemDto create(@RequestBody InventoryItemDto dto) {
        dto.setId(null); // new entity
        return service.create(dto);
    }

    @PutMapping("/{id}")
    public InventoryItemDto update(@PathVariable Long id, @RequestBody InventoryItemDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}