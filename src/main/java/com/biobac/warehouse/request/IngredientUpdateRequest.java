package com.biobac.warehouse.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IngredientUpdateRequest {
    private String name;
    private String description;
    private boolean active;
    private Long ingredientGroupId;
    private Long recipeItemId;
    private Long unitId;
}
