package com.biobac.warehouse.dto;

import lombok.Data;
import java.util.List;

@Data
public class IngredientDto {
    private Long id;
    private String name;
    private String description;
    private String unit;
    private boolean active;
    private Long groupId;
    private Integer initialQuantity;
    private Long warehouseId;
    private Long parentIngredientId;
    private List<Long> childIngredientIds;
}
