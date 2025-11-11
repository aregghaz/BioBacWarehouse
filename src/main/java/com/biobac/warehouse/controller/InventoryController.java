package com.biobac.warehouse.controller;

import com.biobac.warehouse.dto.ChangeComponentDto;
import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.entity.ComponentType;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.ApiResponse;
import com.biobac.warehouse.response.InventoryResponse;
import com.biobac.warehouse.service.InventoryService;
import com.biobac.warehouse.utils.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService inventoryService;

    @PostMapping("/ingredient")
    public ApiResponse<String> changeIngredient(@RequestBody List<ChangeComponentDto> request) {
        return ResponseUtil.success("Not Implemented");
    }

    @PostMapping("/product")
    public ApiResponse<String> changeProduct(@RequestBody List<ChangeComponentDto> request) {
        return ResponseUtil.success("Not Implemented");
    }

    @PostMapping("/ingredient/all")
    public ApiResponse<List<InventoryResponse>> getIngredientAll(@RequestParam(required = false, defaultValue = "0") Integer page,
                                                                 @RequestParam(required = false, defaultValue = "10") Integer size,
                                                                 @RequestParam(required = false, defaultValue = "id") String sortBy,
                                                                 @RequestParam(required = false, defaultValue = "asc") String sortDir,
                                                                 @RequestBody Map<String, FilterCriteria> filters) {
        Pair<List<InventoryResponse>, PaginationMetadata> result = inventoryService.getPagination(filters, page, size, sortBy, sortDir, ComponentType.INGREDIENT);
        return ResponseUtil.success("Transfers retrieved successfully", result.getFirst(), result.getSecond());
    }

    @PostMapping("/product/all")
    public ApiResponse<List<InventoryResponse>> getProductAll(@RequestParam(required = false, defaultValue = "0") Integer page,
                                                              @RequestParam(required = false, defaultValue = "10") Integer size,
                                                              @RequestParam(required = false, defaultValue = "id") String sortBy,
                                                              @RequestParam(required = false, defaultValue = "asc") String sortDir,
                                                              @RequestBody Map<String, FilterCriteria> filters) {
        Pair<List<InventoryResponse>, PaginationMetadata> result = inventoryService.getPagination(filters, page, size, sortBy, sortDir, ComponentType.PRODUCT);
        return ResponseUtil.success("Transfers retrieved successfully", result.getFirst(), result.getSecond());
    }
}
