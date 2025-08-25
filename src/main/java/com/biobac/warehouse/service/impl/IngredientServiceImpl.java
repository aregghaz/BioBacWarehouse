package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.entity.*;
import com.biobac.warehouse.exception.NotFoundException;
import com.biobac.warehouse.repository.*;
import com.biobac.warehouse.request.IngredientCreateRequest;
import com.biobac.warehouse.response.IngredientResponse;
import com.biobac.warehouse.response.InventoryItemResponse;
import com.biobac.warehouse.service.IngredientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IngredientServiceImpl implements IngredientService {
    private final IngredientRepository ingredientRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final RecipeItemRepository recipeItemRepository;
    private final WarehouseRepository warehouseRepository;
    private final IngredientGroupRepository ingredientGroupRepository;

    @Override
    public IngredientResponse create(IngredientCreateRequest request) {
        Ingredient ingredient = new Ingredient();
        ingredient.setName(request.getName());
        ingredient.setActive(request.isActive());
        ingredient.setDescription(request.getDescription());
        InventoryItem inventoryItem = new InventoryItem();

        if (request.getGroupId() != null) {
            IngredientGroup ingredientGroup = ingredientGroupRepository.findById(request.getGroupId())
                    .orElseThrow(() -> new NotFoundException("Ingredient group not found"));
            ingredient.setGroup(ingredientGroup);
            inventoryItem.setIngredientGroup(ingredientGroup);
        }

        if (request.getRecipeItemId() != null) {
            RecipeItem recipeItem = recipeItemRepository.findById(request.getRecipeItemId())
                    .orElseThrow(() -> new NotFoundException("Recipe not found"));
        }

        inventoryItem.setQuantity(request.getQuantity());
        inventoryItem.setIngredient(ingredient);
        inventoryItem.setLastUpdated(LocalDateTime.now());
        if (request.getWarehouseId() != null) {
            Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                    .orElseThrow(() -> new NotFoundException("Warehouse not found"));
            inventoryItem.setWarehouse(warehouse);
        }
        ingredient.getInventoryItems().add(inventoryItem);
        Ingredient saved = ingredientRepository.save(ingredient);
        inventoryItemRepository.save(inventoryItem);

        return toResponse(saved);
    }

    private IngredientResponse toResponse(Ingredient ingredient) {
        IngredientResponse response = new IngredientResponse();
        response.setId(ingredient.getId());
        response.setName(ingredient.getName());
        response.setDescription(ingredient.getDescription());
        response.setActive(ingredient.isActive());

        if (ingredient.getGroup() != null) {
            response.setGroupId(ingredient.getGroup().getId());
        }

        List<InventoryItemResponse> inventoryResponses = ingredient.getInventoryItems().stream()
                .map(item -> {
                    InventoryItemResponse ir = new InventoryItemResponse();
                    ir.setId(item.getId());
                    ir.setQuantity(item.getQuantity());
                    ir.setLastUpdated(item.getLastUpdated());
                    if (item.getWarehouse() != null) {
                        ir.setWarehouseId(item.getWarehouse().getId());
                    }
                    return ir;
                })
                .toList();

        response.setInventoryItems(inventoryResponses);

        return response;
    }

}
