package com.biobac.warehouse.dto;

import lombok.Data;

@Data
public class IngredientComponentDto {
    private Long id;
    private Long childIngredientId;
    private String childIngredientName;
    private Double quantity;
}