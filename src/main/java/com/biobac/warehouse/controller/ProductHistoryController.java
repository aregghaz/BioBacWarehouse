package com.biobac.warehouse.controller;

import com.biobac.warehouse.dto.HistoryMetadata;
import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.ApiResponse;
import com.biobac.warehouse.response.ProductHistoryResponse;
import com.biobac.warehouse.response.ProductHistorySingleResponse;
import com.biobac.warehouse.service.ProductHistoryService;
import com.biobac.warehouse.utils.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.biobac.warehouse.utils.DateUtil.parseDates;

@RestController
@RequestMapping("/api/product-history")
@RequiredArgsConstructor
public class ProductHistoryController {

    private final ProductHistoryService productHistoryService;

    @PostMapping("/product/{productId}")
    public ApiResponse<List<ProductHistorySingleResponse>> getHistoryForProduct(@PathVariable Long productId,
                                                                                @RequestParam(required = false, defaultValue = "0") Integer page,
                                                                                @RequestParam(required = false, defaultValue = "10") Integer size,
                                                                                @RequestParam(required = false, defaultValue = "id") String sortBy,
                                                                                @RequestParam(required = false, defaultValue = "asc") String sortDir,
                                                                                @RequestBody Map<String, FilterCriteria> filters) {
        Pair<List<ProductHistorySingleResponse>, PaginationMetadata> result = productHistoryService.getHistoryForProduct(productId, filters, page, size, sortBy, sortDir);
        List<LocalDateTime> dates = parseDates(filters);
        Double total = productHistoryService.getTotalForProduct(productId);
        Double initial = productHistoryService.getInitialForProduct(productId, dates.get(0));
        Double eventual = productHistoryService.getEventualForProduct(productId, dates.get(1));
        Double increase = productHistoryService.getSumOfIncreasedCount(productId, dates.get(0), dates.get(1));
        Double decrease = productHistoryService.getSumOfDecreasedCount(productId, dates.get(0), dates.get(1));
        HistoryMetadata metadata = new HistoryMetadata(result.getSecond(), total, initial, eventual, increase, decrease, null, null);
        return ResponseUtil.success("Product history retrieved successfully", result.getFirst(), metadata);
    }

    @PostMapping("/all")
    public ApiResponse<List<ProductHistoryResponse>> getAll(@RequestParam(required = false, defaultValue = "0") Integer page,
                                                            @RequestParam(required = false, defaultValue = "10") Integer size,
                                                            @RequestParam(required = false, defaultValue = "id") String sortBy,
                                                            @RequestParam(required = false, defaultValue = "asc") String sortDir,
                                                            @RequestBody Map<String, FilterCriteria> filters) {
        Pair<List<ProductHistoryResponse>, PaginationMetadata> result = productHistoryService.getAll(filters, page, size, sortBy, sortDir);
        return ResponseUtil.success("Products history retrieved successfully", result.getFirst(), result.getSecond());
    }
}