package com.biobac.warehouse.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductCreateRequest {
    private String name;
    private String description;
    private String sku;
    private Long recipeItemId;
    private Double quantity;
    private Long warehouseId;
    private Long companyId;
    private Long unitId;
}
