package com.biobac.warehouse.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class IngredientCreateRequest {
    private String name;
    private String description;
    private Long ingredientGroupId;
    private Integer expiration;
    private BigDecimal price;
    private List<Long> attributeGroupIds;
    private Long unitId;
    private List<UnitTypeConfigRequest> unitTypeConfigs;
    private List<AttributeUpsertRequest> attributes;
}
