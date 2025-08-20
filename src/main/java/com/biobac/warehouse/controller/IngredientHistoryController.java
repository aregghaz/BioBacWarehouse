package com.biobac.warehouse.controller;

import com.biobac.warehouse.dto.IngredientHistoryDto;
import com.biobac.warehouse.response.ApiResponse;
import com.biobac.warehouse.service.IngredientHistoryService;
import com.biobac.warehouse.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/ingredient-history")
@RequiredArgsConstructor
public class IngredientHistoryController {

    private final IngredientHistoryService ingredientHistoryService;

    @GetMapping("/ingredient/{ingredientId}")
    public ApiResponse<List<IngredientHistoryDto>> getHistoryForIngredient(@PathVariable Long ingredientId) {
        List<IngredientHistoryDto> ingredientHistoryDtos = ingredientHistoryService.getHistoryForIngredient(ingredientId);
        return ResponseUtil.success("Ingredient history retrieved successfully", ingredientHistoryDtos);
    }

    @GetMapping("/date-range")
    public ApiResponse<List<IngredientHistoryDto>> getHistoryForDateRange(
            @Parameter(description = "Start date (dd/MM/yyyy:HH:mm:ss)", example = "10/10/2025:10:10:10", schema = @Schema(type = "string"))
            @RequestParam
            LocalDateTime startDate,

            @Parameter(description = "Start date (dd/MM/yyyy:HH:mm:ss)", example = "10/10/2025:10:10:10", schema = @Schema(type = "string"))
            @RequestParam
            LocalDateTime endDate) {
        List<IngredientHistoryDto> ingredientHistoryDtos = ingredientHistoryService.getHistoryForDateRange(startDate, endDate);
        return ResponseUtil.success("Ingredient history for date range retrieved successfully", ingredientHistoryDtos);
    }
}