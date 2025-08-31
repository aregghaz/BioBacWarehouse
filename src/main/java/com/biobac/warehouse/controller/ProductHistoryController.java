package com.biobac.warehouse.controller;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.dto.ProductHistoryDto;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.ApiResponse;
import com.biobac.warehouse.service.ProductHistoryService;
import com.biobac.warehouse.utils.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/product-history")
@RequiredArgsConstructor
public class ProductHistoryController {

    private final ProductHistoryService productHistoryService;

    @PostMapping("/product/{productId}")
    public ApiResponse<List<ProductHistoryDto>> getHistoryForProduct(@PathVariable Long productId,
                                                                     @RequestParam(required = false, defaultValue = "0") Integer page,
                                                                     @RequestParam(required = false, defaultValue = "10") Integer size,
                                                                     @RequestParam(required = false, defaultValue = "id") String sortBy,
                                                                     @RequestParam(required = false, defaultValue = "asc") String sortDir,
                                                                     @RequestBody Map<String, FilterCriteria> filters) {
        Pair<List<ProductHistoryDto>, PaginationMetadata> result = productHistoryService.getHistoryForProduct(productId, filters, page, size, sortBy, sortDir);
        return ResponseUtil.success("Product history retrieved successfully", result.getFirst(), result.getSecond());
    }

    @PostMapping("/all")
    public ApiResponse<List<ProductHistoryDto>> getHistory(@RequestParam(required = false, defaultValue = "0") Integer page,
                                                           @RequestParam(required = false, defaultValue = "10") Integer size,
                                                           @RequestParam(required = false, defaultValue = "id") String sortBy,
                                                           @RequestParam(required = false, defaultValue = "asc") String sortDir,
                                                           @RequestBody Map<String, FilterCriteria> filters) {
        Pair<List<ProductHistoryDto>, PaginationMetadata> result = productHistoryService.getHistory(filters, page, size, sortBy, sortDir);
        return ResponseUtil.success("Products history retrieved successfully", result.getFirst(), result.getSecond());
    }
}