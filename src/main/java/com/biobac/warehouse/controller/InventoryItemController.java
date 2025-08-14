package com.biobac.warehouse.controller;

import com.biobac.warehouse.dto.InventoryItemDto;
import com.biobac.warehouse.response.ApiResponse;
import com.biobac.warehouse.service.InventoryService;
import com.biobac.warehouse.utils.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryItemController {
    private final InventoryService service;

    @GetMapping
    public ApiResponse<List<InventoryItemDto>> getAll() {
        List<InventoryItemDto> inventoryItems = service.getAll();
        return ResponseUtil.success("Inventory items retrieved successfully", inventoryItems);
    }

    @GetMapping("/{id}")
    public ApiResponse<InventoryItemDto> getById(@PathVariable Long id) {
        InventoryItemDto inventoryItem = service.getById(id);
        return ResponseUtil.success("Inventory item retrieved successfully", inventoryItem);
    }
    
    @GetMapping("/product/{productId}")
    public ApiResponse<List<InventoryItemDto>> findByProductId(@PathVariable Long productId) {
        List<InventoryItemDto> inventoryItems = service.findByProductId(productId);
        return ResponseUtil.success("Inventory items for product retrieved successfully", inventoryItems);
    }
    
    @GetMapping("/ingredient/{ingredientId}")
    public ApiResponse<List<InventoryItemDto>> findByIngredientId(@PathVariable Long ingredientId) {
        List<InventoryItemDto> inventoryItems = service.findByIngredientId(ingredientId);
        return ResponseUtil.success("Inventory items for ingredient retrieved successfully", inventoryItems);
    }
    
    @GetMapping("/warehouse/{warehouseId}")
    public ApiResponse<List<InventoryItemDto>> findByWarehouseId(@PathVariable Long warehouseId) {
        List<InventoryItemDto> inventoryItems = service.findByWarehouseId(warehouseId);
        return ResponseUtil.success("Inventory items for warehouse retrieved successfully", inventoryItems);
    }
    
    @GetMapping("/group/{groupId}")
    public ApiResponse<List<InventoryItemDto>> findByGroupId(@PathVariable Long groupId) {
        List<InventoryItemDto> inventoryItems = service.findByGroupId(groupId);
        return ResponseUtil.success("Inventory items for group retrieved successfully", inventoryItems);
    }

    @PostMapping
    public ApiResponse<InventoryItemDto> create(@RequestBody InventoryItemDto dto) {
        dto.setId(null); // new entity
        InventoryItemDto inventoryItemDto = service.create(dto);
        return ResponseUtil.success("Inventory item created successfully", inventoryItemDto);
    }

    @PutMapping("/{id}")
    public ApiResponse<InventoryItemDto> update(@PathVariable Long id, @RequestBody InventoryItemDto dto) {
        InventoryItemDto inventoryItemDto = service.update(id, dto);
        return ResponseUtil.success("Inventory item updated successfully", inventoryItemDto);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseUtil.success("Inventory item deleted successfully");
    }
}