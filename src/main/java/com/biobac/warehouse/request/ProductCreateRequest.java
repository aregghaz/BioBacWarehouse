package com.biobac.warehouse.request;

import com.biobac.warehouse.dto.RecipeItemDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProductCreateRequest {
    private String name;
    private String description;
    private String sku;
    private List<Long> ingredientIds;
    private List<RecipeItemDto> recipeItems;
    private Double quantity;
    private Long warehouseId;
    private Long companyId;
}
