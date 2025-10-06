package com.biobac.warehouse.controller;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.request.ManufactureProductRequest;
import com.biobac.warehouse.request.ReceiveIngredientRequest;
import com.biobac.warehouse.response.ApiResponse;
import com.biobac.warehouse.response.InventoryItemResponse;
import com.biobac.warehouse.response.ManufactureProductResponse;
import com.biobac.warehouse.response.ReceiveIngredientResponse;
import com.biobac.warehouse.service.InventoryItemService;
import com.biobac.warehouse.service.ManufactureProductService;
import com.biobac.warehouse.service.ReceiveIngredientService;
import com.biobac.warehouse.utils.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory-item")
@RequiredArgsConstructor
public class InventoryItemController {
    private final InventoryItemService inventoryItemService;
    private final ManufactureProductService productInventoryService;
    private final ReceiveIngredientService receiveIngredientService;

    @PostMapping("/all")
    public ApiResponse<List<InventoryItemResponse>> getAll(@RequestParam(required = false, defaultValue = "0") Integer page,
                                                           @RequestParam(required = false, defaultValue = "10") Integer size,
                                                           @RequestParam(required = false, defaultValue = "id") String sortBy,
                                                           @RequestParam(required = false, defaultValue = "asc") String sortDir,
                                                           @RequestBody Map<String, FilterCriteria> filters) {
        Pair<List<InventoryItemResponse>, PaginationMetadata> result = inventoryItemService.getAll(filters, page, size, sortBy, sortDir);
        return ResponseUtil.success("Inventory items retrieved successfully", result.getFirst(), result.getSecond());
    }

    @PostMapping("/product")
    public ApiResponse<ManufactureProductResponse> createForProduct(@RequestBody ManufactureProductRequest request) {
        ManufactureProductResponse response = productInventoryService.createForProduct(request);
        return ResponseUtil.success("Inventory item created successfully", response);
    }

    @PostMapping("/ingredient")
    public ApiResponse<List<ReceiveIngredientResponse>> createForIngredient(@RequestBody List<ReceiveIngredientRequest> request) {
        List<ReceiveIngredientResponse> response = receiveIngredientService.createForIngredient(request);
        return ResponseUtil.success("Inventory item created successfully", response);
    }

    @PostMapping("/product/all")
    public ApiResponse<List<ManufactureProductResponse>> getByProductId(@RequestParam Long productId,
                                                                        @RequestParam(required = false, defaultValue = "0") Integer page,
                                                                        @RequestParam(required = false, defaultValue = "10") Integer size,
                                                                        @RequestParam(required = false, defaultValue = "id") String sortBy,
                                                                        @RequestParam(required = false, defaultValue = "asc") String sortDir,
                                                                        @RequestBody Map<String, FilterCriteria> filters) {
        Pair<List<ManufactureProductResponse>, PaginationMetadata> result = productInventoryService.getByProductId(productId, filters, page, size, sortBy, sortDir);
        return ResponseUtil.success("Inventory items retrieved successfully", result.getFirst(), result.getSecond());
    }

    @PostMapping("/ingredient/all")
    public ApiResponse<List<ReceiveIngredientResponse>> getByIngredientId(@RequestParam Long ingredientId,
                                                                          @RequestParam(required = false, defaultValue = "0") Integer page,
                                                                          @RequestParam(required = false, defaultValue = "10") Integer size,
                                                                          @RequestParam(required = false, defaultValue = "id") String sortBy,
                                                                          @RequestParam(required = false, defaultValue = "asc") String sortDir,
                                                                          @RequestBody Map<String, FilterCriteria> filters) {
        Pair<List<ReceiveIngredientResponse>, PaginationMetadata> result = receiveIngredientService.getByIngredientId(ingredientId, filters, page, size, sortBy, sortDir);
        return ResponseUtil.success("Inventory items retrieved successfully", result.getFirst(), result.getSecond());
    }

    @PostMapping("/warehouse/all")
    public ApiResponse<List<InventoryItemResponse>> getByWarehouseId(@RequestParam Long warehouseId,
                                                                     @RequestParam(required = false, defaultValue = "0") Integer page,
                                                                     @RequestParam(required = false, defaultValue = "10") Integer size,
                                                                     @RequestParam(required = false, defaultValue = "id") String sortBy,
                                                                     @RequestParam(required = false, defaultValue = "asc") String sortDir,
                                                                     @RequestBody Map<String, FilterCriteria> filters) {
        Pair<List<InventoryItemResponse>, PaginationMetadata> result = inventoryItemService.getByWarehouseId(warehouseId, filters, page, size, sortBy, sortDir);
        return ResponseUtil.success("Inventory items retrieved successfully", result.getFirst(), result.getSecond());
    }
}
