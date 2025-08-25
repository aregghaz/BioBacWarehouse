package com.biobac.warehouse.controller;

import com.biobac.warehouse.dto.InventoryItemDto;
import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.entity.Entities;
import com.biobac.warehouse.mapper.InventoryMapper;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.request.InventoryItemCreateRequest;
import com.biobac.warehouse.request.InventoryItemUpdateRequest;
import com.biobac.warehouse.response.ApiResponse;
import com.biobac.warehouse.response.InventoryItemResponse;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryItemController extends BaseController {
    private final InventoryService service;
    private final AuditLogService auditLogService;
    private final InventoryMapper inventoryMapper;

    @GetMapping
    public ApiResponse<List<InventoryItemResponse>> getAll() {
        List<InventoryItemResponse> responses = service.getAll().stream()
                .map(inventoryMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseUtil.success("Inventory items retrieved successfully", responses);
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
    public ApiResponse<InventoryItemResponse> getById(@PathVariable Long id) {
        InventoryItemDto inventoryItem = service.getById(id);
        return ResponseUtil.success("Inventory item retrieved successfully", inventoryMapper.toResponse(inventoryItem));
    }

    @GetMapping("/product/{productId}")
    public ApiResponse<List<InventoryItemResponse>> findByProductId(@PathVariable Long productId) {
        List<InventoryItemResponse> responses = service.findByProductId(productId).stream()
                .map(inventoryMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseUtil.success("Inventory items for product retrieved successfully", responses);
    }

    @GetMapping("/ingredient/{ingredientId}")
    public ApiResponse<List<InventoryItemResponse>> findByIngredientId(@PathVariable Long ingredientId) {
        List<InventoryItemResponse> responses = service.findByIngredientId(ingredientId).stream()
                .map(inventoryMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseUtil.success("Inventory items for ingredient retrieved successfully", responses);
    }

    @GetMapping("/warehouse/{warehouseId}")
    public ApiResponse<List<InventoryItemResponse>> findByWarehouseId(@PathVariable Long warehouseId) {
        List<InventoryItemResponse> responses = service.findByWarehouseId(warehouseId).stream()
                .map(inventoryMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseUtil.success("Inventory items for warehouse retrieved successfully", responses);
    }

    @GetMapping("/group/{groupId}")
    public ApiResponse<List<InventoryItemResponse>> findByGroupId(@PathVariable Long groupId) {
        List<InventoryItemResponse> responses = service.findByGroupId(groupId).stream()
                .map(inventoryMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseUtil.success("Inventory items for group retrieved successfully", responses);
    }

    @PostMapping
    public ApiResponse<InventoryItemResponse> create(@RequestBody InventoryItemCreateRequest dto, HttpServletRequest request) {
        InventoryItemDto created = service.create(dto);
        auditLogService.logCreate(Entities.INVENTORYITEM.name(), created.getId(), dto, getUsername(request));
        return ResponseUtil.success("Inventory item created successfully", inventoryMapper.toResponse(created));
    }

    @PutMapping("/{id}")
    public ApiResponse<InventoryItemResponse> update(@PathVariable Long id, @RequestBody InventoryItemUpdateRequest dto, HttpServletRequest request) {
        InventoryItemDto existingItem = service.getById(id);
        InventoryItemDto updated = service.update(id, dto);
        auditLogService.logUpdate(Entities.INVENTORYITEM.name(), updated.getId(), existingItem, dto, getUsername(request));
        return ResponseUtil.success("Inventory item updated successfully", inventoryMapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id, HttpServletRequest request) {
        service.delete(id);
        auditLogService.logDelete(Entities.INVENTORYITEM.name(), id, getUsername(request));
        return ResponseUtil.success("Inventory item deleted successfully");
    }
}