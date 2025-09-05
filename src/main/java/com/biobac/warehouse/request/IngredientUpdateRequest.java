package com.biobac.warehouse.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class IngredientUpdateRequest {
    private String name;
    private String description;
    private boolean active;
    private Long ingredientGroupId;
    private Long recipeItemId;
    private Long unitId;
    private List<UnitTypeConfigRequest> unitTypeConfigs;
    private List<AttributeUpsertRequest> attributes;
}
