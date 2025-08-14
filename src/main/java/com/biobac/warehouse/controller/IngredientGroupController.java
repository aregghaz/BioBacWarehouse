package com.biobac.warehouse.controller;

import com.biobac.warehouse.dto.IngredientGroupDto;
import com.biobac.warehouse.response.ApiResponse;
import com.biobac.warehouse.service.IngredientGroupService;
import com.biobac.warehouse.utils.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ingredient-groups")
@RequiredArgsConstructor
public class IngredientGroupController {

    private final IngredientGroupService service;

    @GetMapping
    public ApiResponse<List<IngredientGroupDto>> getAll() {
        List<IngredientGroupDto> ingredientGroups = service.getAll();
        return ResponseUtil.success("Ingredient groups retrieved successfully", ingredientGroups);
    }

    @GetMapping("/{id}")
    public ApiResponse<IngredientGroupDto> getById(@PathVariable Long id) {
        IngredientGroupDto ingredientGroup = service.getById(id);
        return ResponseUtil.success("Ingredient group retrieved successfully", ingredientGroup);
    }

    @PostMapping
    public ApiResponse<IngredientGroupDto> create(@RequestBody IngredientGroupDto dto) {
        dto.setId(null); // Ensure it's treated as a new entity
        IngredientGroupDto createdGroup = service.create(dto);
        return ResponseUtil.success("Ingredient group created successfully", createdGroup);
    }

    @PutMapping("/{id}")
    public ApiResponse<IngredientGroupDto> update(@PathVariable Long id, @RequestBody IngredientGroupDto dto) {
        IngredientGroupDto updatedGroup = service.update(id, dto);
        return ResponseUtil.success("Ingredient group updated successfully", updatedGroup);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseUtil.success("Ingredient group deleted successfully");
    }
}