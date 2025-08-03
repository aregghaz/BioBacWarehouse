package com.biobac.warehouse.dto;
import lombok.Data;
import java.util.List;

@Data
public class ProductDto {
    private Long id;
    private String name;
    private String description;
    private String sku;
    private List<Long> ingredientIds;
    private List<RecipeItemDto> recipeItems;
    private Double quantity;
    private Long warehouseId;
}
