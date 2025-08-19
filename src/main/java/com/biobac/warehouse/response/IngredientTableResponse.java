package com.biobac.warehouse.response;

import com.biobac.warehouse.dto.IngredientComponentDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class IngredientTableResponse {
    private Long id;
    private String name;
    private String description;
    private String unit;
    private boolean active;
    private Double quantity;
    private String groupName;
    private String warehouseName;
    private List<IngredientComponentDto> childIngredientComponents;
}
