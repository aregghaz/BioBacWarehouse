package com.biobac.warehouse.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private String sku;
    private List<ExtraComponentsResponse> extraComponents;
    private Double totalQuantity;
    private Long recipeItemId;
    private String recipeItemName;
    private Integer expiration;
    private Long unitId;
    private String unitName;
    private Long productGroupId;
    private Double minimalBalance;
    private Long defaultWarehouseId;
    private String defaultWarehouseName;
    private List<Long> attributeGroupIds;
    private String productGroupName;
    private List<UnitTypeConfigResponse> unitTypeConfigs;
    private List<AttributeResponse> attributes;
    private boolean deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
