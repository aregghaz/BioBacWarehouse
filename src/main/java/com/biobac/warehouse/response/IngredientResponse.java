package com.biobac.warehouse.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
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
    private Integer expiration;
    private List<Long> attributeGroupIds;
    private Long unitId;
    private BigDecimal price;
    private BigDecimal lastPrice;
    private Long lastCompanyId;
    private String lastCompanyName;
    private String unitName;
    private Double totalQuantity;
    private Double minimalBalance;
    private Long defaultWarehouseId;
    private String defaultWarehouseName;
    private List<UnitTypeConfigResponse> unitTypeConfigs;
    private List<AttributeResponse> attributes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
