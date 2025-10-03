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
    @NotNull(message = "Sku is required")
    private String sku;
    private Long recipeItemId;
    private Long unitId;
    private Double minimalBalance;
    private Integer expiration;
    private List<Long> attributeGroupIds;
    private List<ProductAdditionalComponents> extraComponents;
    private Long productGroupId;
    private List<UnitTypeConfigRequest> unitTypeConfigs;
    private List<AttributeUpsertRequest> attributes;
    private Long defaultWarehouseId;
}