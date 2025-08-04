package com.biobac.warehouse.controller;

import com.biobac.warehouse.dto.IngredientHistoryDto;
import com.biobac.warehouse.service.IngredientHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/ingredient-history")
@RequiredArgsConstructor
public class IngredientHistoryController {

    private final IngredientHistoryService ingredientHistoryService;

    @GetMapping("/ingredient/{ingredientId}")
    public ResponseEntity<List<IngredientHistoryDto>> getHistoryForIngredient(@PathVariable Long ingredientId) {
        return ResponseEntity.ok(ingredientHistoryService.getHistoryForIngredient(ingredientId));
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<IngredientHistoryDto>> getHistoryForDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(ingredientHistoryService.getHistoryForDateRange(startDate, endDate));
    }
}