package com.biobac.warehouse.dto;

import lombok.Data;

@Data
public class RecipeItemDto {
    private Long id;
    private Long ingredientId;
    private String ingredientName;
    private String ingredientUnit;
    private Double quantity;
    private String notes;
}