package com.biobac.warehouse.controller;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.request.InventoryIngredientCreateRequest;
import com.biobac.warehouse.request.InventoryProductCreateRequest;
import com.biobac.warehouse.response.ApiResponse;
import com.biobac.warehouse.response.InventoryItemResponse;
import com.biobac.warehouse.service.InventoryItemService;
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

    @PostMapping("/all")
    public ApiResponse<List<InventoryItemResponse>> getAll(@RequestParam(required = false, defaultValue = "0") Integer page,
                                                           @RequestParam(required = false, defaultValue = "10") Integer size,
                                                           @RequestParam(required = false, defaultValue = "id") String sortBy,
                                                           @RequestParam(required = false, defaultValue = "asc") String sortDir,
                                                           @RequestBody Map<String, FilterCriteria> filters){
        Pair<List<InventoryItemResponse>, PaginationMetadata> result = inventoryItemService.getAll(filters, page, size, sortBy, sortDir);
        return ResponseUtil.success("Inventory items retrieved successfully", result.getFirst(), result.getSecond());
    }

    @PostMapping("/product")
    public ApiResponse<InventoryItemResponse> createForProduct(@RequestBody InventoryProductCreateRequest request){
        InventoryItemResponse response = inventoryItemService.createForProduct(request);
        return ResponseUtil.success("Inventory item created successfully", response);
    }

    @PostMapping("/ingredient")
    public ApiResponse<InventoryItemResponse> createForIngredient(@RequestBody InventoryIngredientCreateRequest request){
        InventoryItemResponse response = inventoryItemService.createForIngredient(request);
        return ResponseUtil.success("Inventory item created successfully", response);
    }

    @PostMapping("/product/all")
    public ApiResponse<List<InventoryItemResponse>> getByProductId(@RequestParam Long productId,
                                                                   @RequestParam(required = false, defaultValue = "0") Integer page,
                                                                   @RequestParam(required = false, defaultValue = "10") Integer size,
                                                                   @RequestParam(required = false, defaultValue = "id") String sortBy,
                                                                   @RequestParam(required = false, defaultValue = "asc") String sortDir,
                                                                   @RequestBody Map<String, FilterCriteria> filters){
        Pair<List<InventoryItemResponse>, PaginationMetadata> result = inventoryItemService.getByProductId(productId, filters, page, size, sortBy, sortDir);
        return ResponseUtil.success("Inventory items retrieved successfully", result.getFirst(), result.getSecond());
    }

    @PostMapping("/ingredient/all")
    public ApiResponse<List<InventoryItemResponse>> getByIngredientId(@RequestParam Long ingredientId,
                                                                      @RequestParam(required = false, defaultValue = "0") Integer page,
                                                                      @RequestParam(required = false, defaultValue = "10") Integer size,
                                                                      @RequestParam(required = false, defaultValue = "id") String sortBy,
                                                                      @RequestParam(required = false, defaultValue = "asc") String sortDir,
                                                                      @RequestBody Map<String, FilterCriteria> filters){
        Pair<List<InventoryItemResponse>, PaginationMetadata> result = inventoryItemService.getByIngredientId(ingredientId, filters, page, size, sortBy, sortDir);
        return ResponseUtil.success("Inventory items retrieved successfully", result.getFirst(), result.getSecond());
    }
}
