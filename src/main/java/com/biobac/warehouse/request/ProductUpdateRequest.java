package com.biobac.warehouse.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductUpdateRequest {
    private String name;
    private String description;
    private String sku;
    private Long recipeItemId;
    private Long unitId;
    private Long companyId;
}
