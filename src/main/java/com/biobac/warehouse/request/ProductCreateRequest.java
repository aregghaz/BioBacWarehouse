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
    private Long recipeItemId;
    @NotNull(message = "Recipe is required")
    private Double quantity;
    private Long warehouseId;
    private Long companyId;
    private Long unitId;
    private List<Long> attributeGroupIds;
    private Long productGroupId;
    private List<UnitTypeConfigRequest> unitTypeConfigs;
    private List<AttributeUpsertRequest> attributes;
}
