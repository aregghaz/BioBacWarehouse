package com.biobac.warehouse.controller;

import com.biobac.warehouse.dto.RecipeItemDto;
import com.biobac.warehouse.response.ApiResponse;
import com.biobac.warehouse.service.RecipeItemService;
import com.biobac.warehouse.utils.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recipe-items")
@RequiredArgsConstructor
public class RecipeItemController {

    private final RecipeItemService recipeItemService;

    @GetMapping
    public ApiResponse<List<RecipeItemDto>> getAllRecipeItems() {
        List<RecipeItemDto> recipeItems = recipeItemService.getAllRecipeItems();
        return ResponseUtil.success("Recipe items retrieved successfully", recipeItems);
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

    @PostMapping("/product/{productId}")
    public ApiResponse<RecipeItemDto> createRecipeItem(
            @RequestBody RecipeItemDto recipeItemDto,
            @PathVariable Long productId) {
        RecipeItemDto createdRecipeItem = recipeItemService.createRecipeItem(recipeItemDto, productId);
        return ResponseUtil.success("Recipe item created successfully", createdRecipeItem);
    }

    @PutMapping("/{id}")
    public ApiResponse<RecipeItemDto> updateRecipeItem(
            @PathVariable Long id,
            @RequestBody RecipeItemDto recipeItemDto) {
        RecipeItemDto updatedRecipeItem = recipeItemService.updateRecipeItem(id, recipeItemDto);
        return ResponseUtil.success("Recipe item updated successfully", updatedRecipeItem);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteRecipeItem(@PathVariable Long id) {
        recipeItemService.deleteRecipeItem(id);
        return ResponseUtil.success("Recipe item deleted successfully");
    }
}