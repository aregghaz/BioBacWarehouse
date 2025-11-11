package com.biobac.warehouse.controller;

import com.biobac.warehouse.dto.HistoryMetadata;
import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.entity.Ingredient;
import com.biobac.warehouse.exception.NotFoundException;
import com.biobac.warehouse.repository.IngredientRepository;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.ApiResponse;
import com.biobac.warehouse.response.IngredientHistoryResponse;
import com.biobac.warehouse.response.IngredientHistorySingleResponse;
import com.biobac.warehouse.service.IngredientHistoryService;
import com.biobac.warehouse.utils.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.biobac.warehouse.utils.DateUtil.parseDates;

@RestController
@RequestMapping("/api/ingredient-history")
@RequiredArgsConstructor
public class IngredientHistoryController {

    private final IngredientHistoryService ingredientHistoryService;
    private final IngredientRepository ingredientRepository;

    @PostMapping("/ingredient/{ingredientId}")
    public ApiResponse<List<IngredientHistorySingleResponse>> getHistoryForIngredient(@PathVariable Long ingredientId,
                                                                                      @RequestParam(required = false, defaultValue = "0") Integer page,
                                                                                      @RequestParam(required = false, defaultValue = "10") Integer size,
                                                                                      @RequestParam(required = false, defaultValue = "timestamp") String sortBy,
                                                                                      @RequestParam(required = false, defaultValue = "desc") String sortDir,
                                                                                      @RequestBody Map<String, FilterCriteria> filters) {
        Pair<List<IngredientHistorySingleResponse>, PaginationMetadata> result = ingredientHistoryService.getHistoryForIngredient(ingredientId, filters, page, size, sortBy, sortDir);
        List<LocalDateTime> dates = parseDates(filters);
        Double total = ingredientHistoryService.getTotalForIngredient(ingredientId);
        Double initial = ingredientHistoryService.getInitialForIngredient(ingredientId, dates.get(0));
        Double eventual = ingredientHistoryService.getEventualForIngredient(ingredientId, dates.get(1));
        Double increase = ingredientHistoryService.getSumOfIncreasedCount(ingredientId, dates.get(0), dates.get(1));
        Double decrease = ingredientHistoryService.getSumOfDecreasedCount(ingredientId, dates.get(0), dates.get(1));
        Ingredient ingredient = ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> new NotFoundException("Ingredient not found"));
        String ingredientName = ingredient.getName();
        String unitName = ingredient.getUnit().getName();
        HistoryMetadata metadata = new HistoryMetadata(result.getSecond(), total, initial, eventual, increase, decrease, unitName, ingredientName);
        return ResponseUtil.success("Ingredient history retrieved successfully", result.getFirst(), metadata);
    }

    @PostMapping("/all")
    public ApiResponse<List<IngredientHistoryResponse>> getAll(@RequestParam(required = false, defaultValue = "0") Integer page,
                                                               @RequestParam(required = false, defaultValue = "10") Integer size,
                                                               @RequestParam(required = false, defaultValue = "id") String sortBy,
                                                               @RequestParam(required = false, defaultValue = "asc") String sortDir,
                                                               @RequestBody Map<String, FilterCriteria> filters) {
        Pair<List<IngredientHistoryResponse>, PaginationMetadata> result = ingredientHistoryService.getAll(filters, page, size, sortBy, sortDir);
        return ResponseUtil.success("Ingredients history retrieved successfully", result.getFirst(), result.getSecond());
    }
}