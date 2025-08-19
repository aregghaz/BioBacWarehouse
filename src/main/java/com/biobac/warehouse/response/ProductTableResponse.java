package com.biobac.warehouse.response;

import com.biobac.warehouse.dto.RecipeItemDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProductTableResponse {
    private Long id;
    private String name;
    private String description;
    private String sku;
    private List<Long> ingredientIds;
    private List<RecipeItemDto> recipeItems;
    private Double quantity;
    private String warehouseName;
}
