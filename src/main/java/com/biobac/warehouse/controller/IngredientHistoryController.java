package com.biobac.warehouse.controller;

import com.biobac.warehouse.dto.IngredientHistoryDto;
import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.ApiResponse;
import com.biobac.warehouse.service.IngredientHistoryService;
import com.biobac.warehouse.utils.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ingredient-history")
@RequiredArgsConstructor
public class IngredientHistoryController {

    private final IngredientHistoryService ingredientHistoryService;

    @PostMapping("/ingredient/{ingredientId}")
    public ApiResponse<List<IngredientHistoryDto>> getHistoryForIngredient(@PathVariable Long ingredientId,
                                                                           @RequestParam(required = false, defaultValue = "0") Integer page,
                                                                           @RequestParam(required = false, defaultValue = "10") Integer size,
                                                                           @RequestParam(required = false, defaultValue = "id") String sortBy,
                                                                           @RequestParam(required = false, defaultValue = "asc") String sortDir,
                                                                           @RequestBody Map<String, FilterCriteria> filters) {
        Pair<List<IngredientHistoryDto>, PaginationMetadata> result = ingredientHistoryService.getHistoryForIngredient(ingredientId, filters, page, size, sortBy, sortDir);
        return ResponseUtil.success("Ingredient history retrieved successfully", result.getFirst(), result.getSecond());
    }

    @PostMapping("/all")
    public ApiResponse<List<IngredientHistoryDto>> getHistoryForDateRange(@RequestParam(required = false, defaultValue = "0") Integer page,
                                                                          @RequestParam(required = false, defaultValue = "10") Integer size,
                                                                          @RequestParam(required = false, defaultValue = "id") String sortBy,
                                                                          @RequestParam(required = false, defaultValue = "asc") String sortDir,
                                                                          @RequestBody Map<String, FilterCriteria> filters) {
        Pair<List<IngredientHistoryDto>, PaginationMetadata> result = ingredientHistoryService.getHistory(filters, page, size, sortBy, sortDir);
        return ResponseUtil.success("Ingredients history retrieved successfully", result.getFirst(), result.getSecond());
    }
}