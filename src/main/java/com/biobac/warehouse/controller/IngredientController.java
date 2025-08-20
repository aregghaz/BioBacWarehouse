package com.biobac.warehouse.controller;


import com.biobac.warehouse.dto.IngredientDto;
import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.entity.Entities;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.ApiResponse;
import com.biobac.warehouse.response.IngredientTableResponse;
import com.biobac.warehouse.service.AuditLogService;
import com.biobac.warehouse.service.IngredientService;
import com.biobac.warehouse.utils.ResponseUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ingredients")
@RequiredArgsConstructor
public class IngredientController extends BaseController {
    private final AuditLogService auditLogService;
    private final IngredientService ingredientService;

    @GetMapping
    public ApiResponse<List<IngredientDto>> getAll() {
        List<IngredientDto> ingredients = ingredientService.getAll();
        return ResponseUtil.success("Ingredients retrieved successfully", ingredients);
    }

    @PostMapping("/all")
    public ApiResponse<List<IngredientTableResponse>> getAll(@RequestParam(required = false, defaultValue = "0") Integer page,
                                                             @RequestParam(required = false, defaultValue = "10") Integer size,
                                                             @RequestParam(required = false, defaultValue = "id") String sortBy,
                                                             @RequestParam(required = false, defaultValue = "asc") String sortDir,
                                                             @RequestBody Map<String, FilterCriteria> filters) {
        Pair<List<IngredientTableResponse>, PaginationMetadata> result =
                ingredientService.getPagination(filters, page, size, sortBy, sortDir);
        return ResponseUtil.success("Ingredients retrieved successfully", result.getFirst(), result.getSecond());
    }

    @GetMapping("/{id}")
    public ApiResponse<IngredientDto> getById(@PathVariable Long id) {
        IngredientDto ingredient = ingredientService.getById(id);
        return ResponseUtil.success("Ingredient retrieved successfully", ingredient);
    }

    @PostMapping
    public ApiResponse<IngredientDto> create(@RequestBody IngredientDto dto, HttpServletRequest request) {
        dto.setId(null); // Ensure it's treated as a new entity
        IngredientDto createdIngredient = ingredientService.create(dto);
        auditLogService.logCreate(Entities.INGREDIENT.name(), createdIngredient.getId(), dto, getUsername(request));
        return ResponseUtil.success("Ingredient created successfully", createdIngredient);
    }

    @PutMapping("/{id}")
    public ApiResponse<IngredientDto> update(@PathVariable Long id, @RequestBody IngredientDto dto, HttpServletRequest request) {
        IngredientDto existingIngredient = ingredientService.getById(id);
        IngredientDto updatedIngredient = ingredientService.update(id, dto);
        auditLogService.logUpdate(Entities.INGREDIENT.name(), updatedIngredient.getId(), existingIngredient, dto, getUsername(request));
        return ResponseUtil.success("Ingredient updated successfully", updatedIngredient);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        ingredientService.delete(id);
        return ResponseUtil.success("Ingredient deleted successfully");
    }
}
