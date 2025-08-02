package com.biobac.warehouse.controller;

import com.biobac.warehouse.dto.RecipeItemDto;
import com.biobac.warehouse.service.RecipeItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recipe-items")
@RequiredArgsConstructor
public class RecipeItemController {

    private final RecipeItemService recipeItemService;

    @GetMapping
    public ResponseEntity<List<RecipeItemDto>> getAllRecipeItems() {
        return ResponseEntity.ok(recipeItemService.getAllRecipeItems());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecipeItemDto> getRecipeItemById(@PathVariable Long id) {
        return ResponseEntity.ok(recipeItemService.getRecipeItemById(id));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<RecipeItemDto>> getRecipeItemsByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(recipeItemService.getRecipeItemsByProductId(productId));
    }

    @GetMapping("/ingredient/{ingredientId}")
    public ResponseEntity<List<RecipeItemDto>> getRecipeItemsByIngredientId(@PathVariable Long ingredientId) {
        return ResponseEntity.ok(recipeItemService.getRecipeItemsByIngredientId(ingredientId));
    }

    @PostMapping("/product/{productId}")
    public ResponseEntity<RecipeItemDto> createRecipeItem(
            @RequestBody RecipeItemDto recipeItemDto,
            @PathVariable Long productId) {
        RecipeItemDto createdRecipeItem = recipeItemService.createRecipeItem(recipeItemDto, productId);
        return new ResponseEntity<>(createdRecipeItem, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecipeItemDto> updateRecipeItem(
            @PathVariable Long id,
            @RequestBody RecipeItemDto recipeItemDto) {
        return ResponseEntity.ok(recipeItemService.updateRecipeItem(id, recipeItemDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipeItem(@PathVariable Long id) {
        recipeItemService.deleteRecipeItem(id);
        return ResponseEntity.noContent().build();
    }
}