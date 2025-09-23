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
    private Double totalQuantity;
    private Long recipeItemId;
    private String recipeItemName;
    private Integer expiration;
    private Long unitId;
    private String unitName;
    private Long productGroupId;
    private List<Long> attributeGroupIds;
    private String productGroupName;
    private List<InventoryItemResponse> inventoryItems;
    private List<UnitTypeConfigResponse> unitTypeConfigs;
    private List<AttributeResponse> attributes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
