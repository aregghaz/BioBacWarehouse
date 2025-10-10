package com.biobac.warehouse.controller;

import com.biobac.warehouse.dto.HistoryMetadata;
import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.entity.Ingredient;
import com.biobac.warehouse.exception.NotFoundException;
import com.biobac.warehouse.repository.IngredientRepository;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.ApiResponse;
import com.biobac.warehouse.response.IngredientHistoryResponse;
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
    private final IngredientRepository ingredientRepository;

    @PostMapping("/ingredient/{ingredientId}")
    public ApiResponse<List<IngredientHistoryResponse>> getHistoryForIngredient(@PathVariable Long ingredientId,
                                                                                @RequestParam(required = false, defaultValue = "0") Integer page,
                                                                                @RequestParam(required = false, defaultValue = "10") Integer size,
                                                                                @RequestParam(required = false, defaultValue = "id") String sortBy,
                                                                                @RequestParam(required = false, defaultValue = "asc") String sortDir,
                                                                                @RequestBody Map<String, FilterCriteria> filters) {
        Pair<List<IngredientHistoryResponse>, PaginationMetadata> result = ingredientHistoryService.getHistoryForIngredient(ingredientId, filters, page, size, sortBy, sortDir);
        Double total = ingredientHistoryService.getTotalForIngredient(ingredientId, filters);
        Ingredient ingredient = ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> new NotFoundException("Ingredient not found"));
        String unitName = ingredient.getUnit().getName();
        HistoryMetadata metadata = new HistoryMetadata(result.getSecond(), total, unitName);
        return ResponseUtil.success("Ingredient history retrieved successfully", result.getFirst(), metadata);
    }

    @PostMapping("/all")
    public ApiResponse<List<IngredientHistoryResponse>> getHistoryForDateRange(@RequestParam(required = false, defaultValue = "0") Integer page,
                                                                               @RequestParam(required = false, defaultValue = "10") Integer size,
                                                                               @RequestParam(required = false, defaultValue = "id") String sortBy,
                                                                               @RequestParam(required = false, defaultValue = "asc") String sortDir,
                                                                               @RequestBody Map<String, FilterCriteria> filters) {
        Pair<List<IngredientHistoryResponse>, PaginationMetadata> result = ingredientHistoryService.getHistory(filters, page, size, sortBy, sortDir);
        return ResponseUtil.success("Ingredients history retrieved successfully", result.getFirst(), result.getSecond());
    }
}