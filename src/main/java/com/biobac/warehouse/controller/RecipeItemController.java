package com.biobac.warehouse.controller;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.dto.RecipeItemDto;
import com.biobac.warehouse.dto.WarehouseDto;
import com.biobac.warehouse.entity.Entities;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.request.RecipeItemCreateRequest;
import com.biobac.warehouse.response.ApiResponse;
import com.biobac.warehouse.response.RecipeItemResponse;
import com.biobac.warehouse.response.RecipeItemTableResponse;
import com.biobac.warehouse.service.AuditLogService;
import com.biobac.warehouse.service.RecipeItemService;
import com.biobac.warehouse.utils.ResponseUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recipe-items")
@RequiredArgsConstructor
public class RecipeItemController extends BaseController {
    private final AuditLogService auditLogService;
    private final RecipeItemService recipeItemService;

    @GetMapping
    public ApiResponse<List<RecipeItemDto>> getAll() {
        List<RecipeItemDto> recipeItemDtos = recipeItemService.getAll();
        return ResponseUtil.success("Recipe Items retrieved successfully", recipeItemDtos);
    }


    @PostMapping("/all")
    public ApiResponse<List<RecipeItemTableResponse>> getAllRecipeItems(@RequestParam(required = false, defaultValue = "0") Integer page,
                                                                        @RequestParam(required = false, defaultValue = "10") Integer size,
                                                                        @RequestParam(required = false, defaultValue = "id") String sortBy,
                                                                        @RequestParam(required = false, defaultValue = "asc") String sortDir,
                                                                        @RequestBody Map<String, FilterCriteria> filters) {
        Pair<List<RecipeItemTableResponse>, PaginationMetadata> result = recipeItemService.getPagination(filters, page, size, sortBy, sortDir);
        return ResponseUtil.success("Recipe items retrieved successfully", result.getFirst(), result.getSecond());
    }

    @GetMapping("/{id}")
    public ApiResponse<RecipeItemDto> getRecipeItemById(@PathVariable Long id) {
        RecipeItemDto recipeItem = recipeItemService.getRecipeItemById(id);
        return ResponseUtil.success("Recipe item retrieved successfully", recipeItem);
    }

    @GetMapping("/product/{productId}")
    public ApiResponse<List<RecipeItemDto>> getRecipeItemsByProductId(@PathVariable Long productId) {
        List<RecipeItemDto> recipeItems = recipeItemService.getRecipeItemsByProductId(productId);
        return ResponseUtil.success("Recipe items for product retrieved successfully", recipeItems);
    }

    @GetMapping("/ingredient/{ingredientId}")
    public ApiResponse<List<RecipeItemDto>> getRecipeItemsByIngredientId(@PathVariable Long ingredientId) {
        List<RecipeItemDto> recipeItems = recipeItemService.getRecipeItemsByIngredientId(ingredientId);
        return ResponseUtil.success("Recipe items for ingredient retrieved successfully", recipeItems);
    }

    @PostMapping
    public ApiResponse<RecipeItemResponse> createRecipeItem(
            @RequestBody RecipeItemCreateRequest recipeItemDto,
            HttpServletRequest request) {
        RecipeItemResponse createdRecipeItem = recipeItemService.createRecipeItem(recipeItemDto);
        auditLogService.logCreate(Entities.RECIPEITEM.name(), createdRecipeItem.getId(), recipeItemDto, getUsername(request));

        return ResponseUtil.success("Recipe item created successfully", createdRecipeItem);
    }

    @PutMapping("/{id}")
    public ApiResponse<RecipeItemDto> updateRecipeItem(
            @PathVariable Long id,
            @RequestBody RecipeItemDto recipeItemDto,
            HttpServletRequest request) {
        RecipeItemDto existingRecipeItem = recipeItemService.getRecipeItemById(id);
        RecipeItemDto updatedRecipeItem = recipeItemService.updateRecipeItem(id, recipeItemDto);
        auditLogService.logUpdate(Entities.RECIPEITEM.name(), updatedRecipeItem.getId(), existingRecipeItem, recipeItemDto, getUsername(request));
        return ResponseUtil.success("Recipe item updated successfully", updatedRecipeItem);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteRecipeItem(@PathVariable Long id, HttpServletRequest request) {
        recipeItemService.deleteRecipeItem(id);
        auditLogService.logDelete(Entities.RECIPEITEM.name(), id, getUsername(request));
        return ResponseUtil.success("Recipe item deleted successfully");
    }
}