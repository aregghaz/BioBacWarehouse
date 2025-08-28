package com.biobac.warehouse.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IngredientCreateRequest {
    private String name;
    private String description;
    private boolean active;
    private Long groupId;
    private Long recipeItemId;
    private Double quantity;
    private Long warehouseId;
    private Long unitId;
}
