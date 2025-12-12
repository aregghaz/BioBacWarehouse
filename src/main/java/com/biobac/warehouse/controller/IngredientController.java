package com.biobac.warehouse.controller;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.request.IngredientCreateRequest;
import com.biobac.warehouse.request.IngredientUpdateRequest;
import com.biobac.warehouse.response.ApiResponse;
import com.biobac.warehouse.response.IngredientResponse;
import com.biobac.warehouse.service.IngredientService;
import com.biobac.warehouse.utils.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/warehouse/ingredient")
@RequiredArgsConstructor
public class IngredientController {
    private final IngredientService ingredientService;

    @PostMapping
    public ApiResponse<IngredientResponse> create(@RequestBody IngredientCreateRequest ingredient) {
        IngredientResponse saved = ingredientService.create(ingredient);
        return ResponseUtil.success("Ingredient created successfully", saved);
    }

    @GetMapping("/{id}")
    public ApiResponse<IngredientResponse> getById(@PathVariable Long id) {
        IngredientResponse ing = ingredientService.getById(id);
        return ResponseUtil.success("Ingredient retrieved successfully", ing);
    }

    @GetMapping
    public ApiResponse<List<IngredientResponse>> getAll() {
        List<IngredientResponse> list = ingredientService.getAll();
        return ResponseUtil.success("Ingredients retrieved successfully", list);
    }

    @PostMapping("/all")
    public ApiResponse<List<IngredientResponse>> getAllIngredients(@RequestParam(required = false, defaultValue = "0") Integer page,
                                                                   @RequestParam(required = false, defaultValue = "10") Integer size,
                                                                   @RequestParam(required = false, defaultValue = "id") String sortBy,
                                                                   @RequestParam(required = false, defaultValue = "asc") String sortDir,
                                                                   @RequestBody Map<String, FilterCriteria> filters) {
        Pair<List<IngredientResponse>, PaginationMetadata> result = ingredientService.getPagination(filters, page, size, sortBy, sortDir);
        return ResponseUtil.success("Ingredients retrieved successfully", result.getFirst(), result.getSecond());
    }

    @PutMapping("/{id}")
    public ApiResponse<IngredientResponse> update(@PathVariable Long id, @RequestBody IngredientUpdateRequest ingredient) {
        IngredientResponse updated = ingredientService.update(id, ingredient);
        return ResponseUtil.success("Ingredient updated successfully", updated);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        ingredientService.delete(id);
        return ResponseUtil.success("Ingredient deleted successfully");
    }
}
