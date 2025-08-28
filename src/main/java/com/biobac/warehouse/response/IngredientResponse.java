package com.biobac.warehouse.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class IngredientResponse {
    private Long id;
    private String name;
    private String description;
    private boolean active;
    private Long ingredientGroupId;
    private String ingredientGroupName;
    private String recipeItemName;
    private Long unitId;
    private String unitName;
    private Double totalQuantity;
    private List<InventoryItemResponse> inventoryItems;
}
