package com.biobac.warehouse.controller;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.request.AssetCategoryRequest;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.ApiResponse;
import com.biobac.warehouse.response.AssetCategoryResponse;
import com.biobac.warehouse.service.AssetCategoryService;
import com.biobac.warehouse.utils.ResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/asset-category")
@RequiredArgsConstructor
public class AssetCategoryController {
    private final AssetCategoryService assetCategoryService;

    @PostMapping
    public ApiResponse<AssetCategoryResponse> create(@Valid @RequestBody AssetCategoryRequest request) {
        AssetCategoryResponse response = assetCategoryService.create(request);
        return ResponseUtil.success("Asset Category created successfully", response);
    }

    @GetMapping("/{id}")
    public ApiResponse<AssetCategoryResponse> getById(@PathVariable Long id) {
        AssetCategoryResponse response = assetCategoryService.getById(id);
        return ResponseUtil.success("Asset Category retrieved successfully", response);
    }

    @GetMapping
    public ApiResponse<List<AssetCategoryResponse>> getAll() {
        List<AssetCategoryResponse> responses = assetCategoryService.getAll();
        return ResponseUtil.success("Asset Category retrieved successfully", responses);
    }

    @PostMapping("/all")
    public ApiResponse<List<AssetCategoryResponse>> getAllProducts(@RequestParam(required = false, defaultValue = "0") Integer page,
                                                                   @RequestParam(required = false, defaultValue = "10") Integer size,
                                                                   @RequestParam(required = false, defaultValue = "id") String sortBy,
                                                                   @RequestParam(required = false, defaultValue = "asc") String sortDir,
                                                                   @RequestBody Map<String, FilterCriteria> filters) {
        Pair<List<AssetCategoryResponse>, PaginationMetadata> result = assetCategoryService.getPagination(filters, page, size, sortBy, sortDir);
        return ResponseUtil.success("Asset Category retrieved successfully", result.getFirst(), result.getSecond());
    }

    @PutMapping("/{id}")
    public ApiResponse<AssetCategoryResponse> update(@PathVariable Long id, @RequestBody AssetCategoryRequest request) {
        AssetCategoryResponse response = assetCategoryService.update(id, request);
        return ResponseUtil.success("Asset Category updated successfully", response);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        assetCategoryService.delete(id);
        return ResponseUtil.success("Asset Category deleted successfully");
    }
}
