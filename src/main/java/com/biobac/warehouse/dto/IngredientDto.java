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
    private Double quantity;
    private Long groupId;
    private Long warehouseId;
    private List<IngredientComponentDto> childIngredientComponents;
}
