package com.biobac.warehouse.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProductUpdateRequest {
    private String name;
    private String description;
    private String sku;
    private Integer expiration;
    private Long recipeItemId;
    private Long unitId;
    private Long productGroupId;
    private List<UnitTypeConfigRequest> unitTypeConfigs;
    private List<Long> attributeGroupIds;
    private List<AttributeUpsertRequest> attributes;
    private List<ProductAdditionalComponents> extraComponents;
    private Long defaultWarehouseId;
}
