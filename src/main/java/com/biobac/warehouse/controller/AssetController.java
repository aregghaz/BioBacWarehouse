package com.biobac.warehouse.controller;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.request.AssetRegisterRequest;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.ApiResponse;
import com.biobac.warehouse.response.AssetCategoryResponse;
import com.biobac.warehouse.response.AssetResponse;
import com.biobac.warehouse.service.AssetService;
import com.biobac.warehouse.utils.ResponseUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/asset")
@RequiredArgsConstructor
public class AssetController {
    private final AssetService assetService;

    @PostMapping
    public ApiResponse<AssetResponse> register(@RequestBody AssetRegisterRequest request) {
        AssetResponse response = assetService.register(request);
        return ResponseUtil.success("Asset registered successfully", response);
    }

    @PostMapping("/all")
    public ApiResponse<List<AssetResponse>> getPagination(@RequestParam(required = false, defaultValue = "0") Integer page,
                                                          @RequestParam(required = false, defaultValue = "10") Integer size,
                                                          @RequestParam(required = false, defaultValue = "id") String sortBy,
                                                          @RequestParam(required = false, defaultValue = "asc") String sortDir,
                                                          @RequestBody Map<String, FilterCriteria> filters) {
        Pair<List<AssetResponse>, PaginationMetadata> result = assetService.getPagination(filters, page, size, sortBy, sortDir);
        return ResponseUtil.success("Assets retrieved successfully", result.getFirst(), result.getSecond());
    }

    @PutMapping("/{id}")
    public ApiResponse<AssetResponse> update(@PathVariable Long id, @RequestBody AssetRegisterRequest request) {
        AssetResponse updated = assetService.update(id, request);
        return ResponseUtil.success("Assets updated successfully", updated);
    }

    @GetMapping
    public ApiResponse<List<AssetResponse>> getAll() {
        List<AssetResponse> responses = assetService.getAll();
        return ResponseUtil.success("Assets retrieved successfully", responses);
    }

    @GetMapping("/{id}")
    public ApiResponse<AssetResponse> getById(@PathVariable Long id) {
        AssetResponse response = assetService.getById(id);
        return ResponseUtil.success("Asset retrieved successfully", response);
    }
}
