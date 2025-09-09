package com.biobac.warehouse.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
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
    private Long recipeItemId;
    private String recipeItemName;
    private List<Long> attributeGroupIds;
    private Long unitId;
    private String unitName;
    private Double totalQuantity;
    private List<InventoryItemResponse> inventoryItems;
    private List<UnitTypeConfigResponse> unitTypeConfigs;
    private List<AttributeDefResponse> attributes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
