package com.biobac.warehouse.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProductCreateRequest {
    private String name;
    private String description;
    private String sku;
    @NotNull(message = "Recipe is required")
    private Long recipeItemId;
    private Long unitId;
    private Integer expiration;
    private List<Long> attributeGroupIds;
    private Long productGroupId;
    private List<UnitTypeConfigRequest> unitTypeConfigs;
    private List<AttributeUpsertRequest> attributes;
}
