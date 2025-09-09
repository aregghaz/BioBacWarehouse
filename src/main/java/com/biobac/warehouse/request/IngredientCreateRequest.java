package com.biobac.warehouse.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class IngredientCreateRequest {
    private String name;
    private String description;
    private boolean active;
    private Long groupId;
    private Long recipeItemId;
    private Double quantity;
    private List<Long> attributeGroupIds;
    private Long warehouseId;
    private Long companyId;
    private Long unitId;
    private List<UnitTypeConfigRequest> unitTypeConfigs;
    private List<AttributeUpsertRequest> attributes;
}
