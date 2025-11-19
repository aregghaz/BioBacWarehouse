package com.biobac.warehouse.controller;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.request.ManufactureCalculateRequest;
import com.biobac.warehouse.request.ManufactureProductRequest;
import com.biobac.warehouse.response.ApiResponse;
import com.biobac.warehouse.response.ManufactureCalculateMetadata;
import com.biobac.warehouse.response.ManufactureCalculateResponse;
import com.biobac.warehouse.response.ManufactureProductResponse;
import com.biobac.warehouse.service.ManufactureProductService;
import com.biobac.warehouse.utils.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/manufacture-product")
@RequiredArgsConstructor
public class ManufactureProductController {
    private final ManufactureProductService manufactureProductService;

    @PostMapping
    public ApiResponse<ManufactureProductResponse> createForProduct(@RequestBody ManufactureProductRequest request) {
        ManufactureProductResponse response = manufactureProductService.createForProduct(request);
        return ResponseUtil.success("Product manufactured successfully", response);
    }

    @PostMapping("/calculate")
    public ApiResponse<List<ManufactureCalculateResponse>> calculateProductions(@RequestBody List<ManufactureCalculateRequest> request) {
        Pair<List<ManufactureCalculateResponse>, List<ManufactureCalculateMetadata>> result = manufactureProductService.calculateProductions(request);
        return ResponseUtil.success("Productions calculated successfully", result.getFirst(), result.getSecond());
    }

    @PostMapping("/all")
    public ApiResponse<List<ManufactureProductResponse>> getByProductId(
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDir,
            @RequestBody Map<String, FilterCriteria> filters) {
        Pair<List<ManufactureProductResponse>, PaginationMetadata> result = manufactureProductService.getByProductId(filters, page, size, sortBy, sortDir);
        return ResponseUtil.success("Manufactured Products retrieved successfully", result.getFirst(), result.getSecond());
    }
}
