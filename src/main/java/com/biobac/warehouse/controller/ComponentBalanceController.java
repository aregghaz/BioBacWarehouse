package com.biobac.warehouse.controller;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.*;
import com.biobac.warehouse.service.ComponentBalanceService;
import com.biobac.warehouse.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/component-balance")
@RequiredArgsConstructor
public class ComponentBalanceController {
    private final ComponentBalanceService componentBalanceService;

    @PostMapping("/ingredient/all")
    public ApiResponse<List<ComponentBalanceIngResponse>> getAllIngredients(@RequestParam(required = false, defaultValue = "0") Integer page,
                                                                            @RequestParam(required = false, defaultValue = "10") Integer size,
                                                                            @RequestParam(required = false, defaultValue = "id") String sortBy,
                                                                            @RequestParam(required = false, defaultValue = "asc") String sortDir,
                                                                            @RequestBody Map<String, FilterCriteria> filters) {
        Pair<List<ComponentBalanceIngResponse>, PaginationMetadata> result = componentBalanceService.getIngPagination(filters, page, size, sortBy, sortDir);
        return ResponseUtil.success("Component Balances retrieved successfully", result.getFirst(), result.getSecond());
    }

    @PostMapping("/product/all")
    public ApiResponse<List<ComponentBalanceProdResponse>> getAllProducts(@RequestParam(required = false, defaultValue = "0") Integer page,
                                                                          @RequestParam(required = false, defaultValue = "10") Integer size,
                                                                          @RequestParam(required = false, defaultValue = "id") String sortBy,
                                                                          @RequestParam(required = false, defaultValue = "asc") String sortDir,
                                                                          @RequestBody Map<String, FilterCriteria> filters) {
        Pair<List<ComponentBalanceProdResponse>, PaginationMetadata> result = componentBalanceService.getProdPagination(filters, page, size, sortBy, sortDir);
        return ResponseUtil.success("Component Balances retrieved successfully", result.getFirst(), result.getSecond());
    }

    @PostMapping("/product-details/all")
    public ApiResponse<List<ProductDetailResponse>> getAllByProductId(@RequestParam Long productId,
                                                                      @RequestParam(required = false, defaultValue = "0") Integer page,
                                                                      @RequestParam(required = false, defaultValue = "10") Integer size,
                                                                      @RequestParam(required = false, defaultValue = "id") String sortBy,
                                                                      @RequestParam(required = false, defaultValue = "asc") String sortDir,
                                                                      @RequestBody Map<String, FilterCriteria> filters) {
        Pair<List<ProductDetailResponse>, PaginationMetadata> result = componentBalanceService.getProductDetailsByProductId(productId, filters, page, size, sortBy, sortDir);
        return ResponseUtil.success("Component Balances retrieved successfully", result.getFirst(), result.getSecond());
    }

    @PostMapping("/ingredient-details/all")
    public ApiResponse<List<IngredientDetailResponse>> getAllByIngredientId(@RequestParam Long ingredientId,
                                                                            @RequestParam(required = false, defaultValue = "0") Integer page,
                                                                            @RequestParam(required = false, defaultValue = "10") Integer size,
                                                                            @RequestParam(required = false, defaultValue = "id") String sortBy,
                                                                            @RequestParam(required = false, defaultValue = "asc") String sortDir,
                                                                            @RequestBody Map<String, FilterCriteria> filters) {
        Pair<List<IngredientDetailResponse>, PaginationMetadata> result = componentBalanceService.getIngredientDetailsByIngredientId(ingredientId, filters, page, size, sortBy, sortDir);
        return ResponseUtil.success("Component Balances retrieved successfully", result.getFirst(), result.getSecond());
    }

    @GetMapping("/ingredient-balance")
    public ApiResponse<ComponentBalanceQuantityResponse> getIngredientBalance(@RequestParam Long ingredientId,
                                                                              @RequestParam Long warehouseId,
                                                                              @Parameter(description = "Start date (dd/MM/yyyy:HH:mm:ss)", example = "10/10/2025:10:10:10", schema = @Schema(type = "string")) @RequestParam LocalDateTime date) {
        ComponentBalanceQuantityResponse response = componentBalanceService.getIngredientBalance(ingredientId, warehouseId, date);
        return ResponseUtil.success("Balance retrieved successfully", response);
    }

    @GetMapping("/product-balance")
    public ApiResponse<ComponentBalanceQuantityResponse> getProductBalance(@RequestParam Long productId,
                                                                           @RequestParam Long warehouseId,
                                                                           @Parameter(description = "Start date (dd/MM/yyyy:HH:mm:ss)", example = "10/10/2025:10:10:10", schema = @Schema(type = "string")) @RequestParam LocalDateTime date) {
        ComponentBalanceQuantityResponse response = componentBalanceService.getProductBalance(productId, warehouseId, date);
        return ResponseUtil.success("Balance retrieved successfully", response);
    }

    @GetMapping("/related-ingredients")
    public ApiResponse<List<IngredientResponse>> getRelatedIngredients(@RequestParam Long warehouseId) {
        List<IngredientResponse> responses = componentBalanceService.getRelatedIngredients(warehouseId);
        return ResponseUtil.success("Ingredients retrieved successfully", responses);
    }

    @GetMapping("/related-products")
    public ApiResponse<List<ProductResponse>> getRelatedProducts(@RequestParam Long warehouseId) {
        List<ProductResponse> responses = componentBalanceService.getRelatedProducts(warehouseId);
        return ResponseUtil.success("Products retrieved successfully", responses);
    }
}