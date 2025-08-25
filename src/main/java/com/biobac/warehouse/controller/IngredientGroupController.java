package com.biobac.warehouse.controller;

import com.biobac.warehouse.dto.IngredientGroupDto;
import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.entity.Entities;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.ApiResponse;
import com.biobac.warehouse.response.IngredientGroupTableResponse;
import com.biobac.warehouse.service.AuditLogService;
import com.biobac.warehouse.service.IngredientGroupService;
import com.biobac.warehouse.utils.ResponseUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ingredient-groups")
@RequiredArgsConstructor
public class IngredientGroupController extends BaseController {
    private final AuditLogService auditLogService;
    private final IngredientGroupService service;

    @GetMapping
    public ApiResponse<List<IngredientGroupDto>> getAll() {
        List<IngredientGroupDto> ingredientGroupDtos = service.getPagination();
        return ResponseUtil.success("Ingredient groups retrieved successfully", ingredientGroupDtos);
    }

    @PostMapping("/all")
    public ApiResponse<List<IngredientGroupTableResponse>> getAll(@RequestParam(required = false, defaultValue = "0") Integer page,
                                                                  @RequestParam(required = false, defaultValue = "10") Integer size,
                                                                  @RequestParam(required = false, defaultValue = "id") String sortBy,
                                                                  @RequestParam(required = false, defaultValue = "asc") String sortDir,
                                                                  @RequestBody Map<String, FilterCriteria> filters) {
        Pair<List<IngredientGroupTableResponse>, PaginationMetadata> result = service.getPagination(filters, page, size, sortBy, sortDir);
        return ResponseUtil.success("Ingredient groups retrieved successfully", result.getFirst(), result.getSecond());
    }

    @GetMapping("/{id}")
    public ApiResponse<IngredientGroupDto> getById(@PathVariable Long id) {
        IngredientGroupDto ingredientGroup = service.getById(id);
        return ResponseUtil.success("Ingredient group retrieved successfully", ingredientGroup);
    }

    @PostMapping
    public ApiResponse<IngredientGroupDto> create(@RequestBody IngredientGroupDto dto, HttpServletRequest request) {
        dto.setId(null); // Ensure it's treated as a new entity
        IngredientGroupDto createdGroup = service.create(dto);
        auditLogService.logCreate(Entities.INGREDIENTGROUP.name(), createdGroup.getId(), dto, getUsername(request));

        return ResponseUtil.success("Ingredient group created successfully", createdGroup);
    }

    @PutMapping("/{id}")
    public ApiResponse<IngredientGroupDto> update(@PathVariable Long id, @RequestBody IngredientGroupDto dto, HttpServletRequest request) {
        IngredientGroupDto existingGroup = service.getById(id);
        IngredientGroupDto updatedGroup = service.update(id, dto);
        auditLogService.logUpdate(Entities.INGREDIENTGROUP.name(), updatedGroup.getId(), existingGroup, dto, getUsername(request));
        return ResponseUtil.success("Ingredient group updated successfully", updatedGroup);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id, HttpServletRequest request) {
        service.delete(id);
        auditLogService.logDelete(Entities.INGREDIENTGROUP.name(), id, getUsername(request));
        return ResponseUtil.success("Ingredient group deleted successfully");
    }
}