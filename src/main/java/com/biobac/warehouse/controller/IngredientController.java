package com.biobac.warehouse.controller;


import com.biobac.warehouse.dto.IngredientDto;
import com.biobac.warehouse.response.ApiResponse;
import com.biobac.warehouse.service.IngredientService;
import com.biobac.warehouse.utils.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ingredients")
@RequiredArgsConstructor
public class IngredientController {

    private final IngredientService service;

    @GetMapping
    public ApiResponse<List<IngredientDto>> getAll() {
        List<IngredientDto> ingredients = service.getAll();
        return ResponseUtil.success("Ingredients retrieved successfully", ingredients);
    }

    @GetMapping("/{id}")
    public ApiResponse<IngredientDto> getById(@PathVariable Long id) {
        IngredientDto ingredient = service.getById(id);
        return ResponseUtil.success("Ingredient retrieved successfully", ingredient);
    }

    @PostMapping
    public ApiResponse<IngredientDto> create(@RequestBody IngredientDto dto) {
        dto.setId(null); // Ensure it's treated as a new entity
        IngredientDto createdIngredient = service.create(dto);
        return ResponseUtil.success("Ingredient created successfully", createdIngredient);
    }

    @PutMapping("/{id}")
    public ApiResponse<IngredientDto> update(@PathVariable Long id, @RequestBody IngredientDto dto) {
        IngredientDto updatedIngredient = service.update(id, dto);
        return ResponseUtil.success("Ingredient updated successfully", updatedIngredient);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseUtil.success("Ingredient deleted successfully");
    }
}
