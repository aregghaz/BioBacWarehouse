package com.biobac.warehouse.controller;

import com.biobac.warehouse.dto.InventoryItemDto;
import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.entity.Entities;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.ApiResponse;
import com.biobac.warehouse.response.InventoryItemTableResponse;
import com.biobac.warehouse.service.AuditLogService;
import com.biobac.warehouse.service.InventoryService;
import com.biobac.warehouse.utils.ResponseUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryItemController extends BaseController {
    private final InventoryService service;
    private final AuditLogService auditLogService;

    @GetMapping
    public ApiResponse<List<InventoryItemDto>> getAll() {
        List<InventoryItemDto> inventoryItemDtos = service.getAll();
        return ResponseUtil.success("Inventory items retrieved successfully", inventoryItemDtos);
    }

    @PostMapping("/all")
    public ApiResponse<List<InventoryItemTableResponse>> getAll(@RequestParam(required = false, defaultValue = "0") Integer page,
                                                                @RequestParam(required = false, defaultValue = "10") Integer size,
                                                                @RequestParam(required = false, defaultValue = "id") String sortBy,
                                                                @RequestParam(required = false, defaultValue = "asc") String sortDir,
                                                                @RequestBody Map<String, FilterCriteria> filters) {
        Pair<List<InventoryItemTableResponse>, PaginationMetadata> result = service.getPagination(filters, page, size, sortBy, sortDir);
        return ResponseUtil.success("Inventory items retrieved successfully", result.getFirst(), result.getSecond());
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
    public ApiResponse<InventoryItemDto> create(@RequestBody InventoryItemDto dto, HttpServletRequest request) {
        dto.setId(null); // new entity
        InventoryItemDto inventoryItemDto = service.create(dto);
        auditLogService.logCreate(Entities.INVENTORYITEM.name(), inventoryItemDto.getId(), dto, getUsername(request));
        return ResponseUtil.success("Inventory item created successfully", inventoryItemDto);
    }

    @PutMapping("/{id}")
    public ApiResponse<InventoryItemDto> update(@PathVariable Long id, @RequestBody InventoryItemDto dto, HttpServletRequest request) {
        InventoryItemDto existingItem = service.getById(id);
        InventoryItemDto inventoryItemDto = service.update(id, dto);
        auditLogService.logUpdate(Entities.INVENTORYITEM.name(), inventoryItemDto.getId(), existingItem, dto, getUsername(request));
        return ResponseUtil.success("Inventory item updated successfully", inventoryItemDto);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseUtil.success("Inventory item deleted successfully");
    }
}