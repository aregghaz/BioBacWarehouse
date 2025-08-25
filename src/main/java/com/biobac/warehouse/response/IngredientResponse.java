package com.biobac.warehouse.response;

import com.biobac.warehouse.dto.IngredientComponentDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class IngredientResponse {
    private Long id;
    private String name;
    private String description;
    private boolean active;
    private Long groupId;

    private List<InventoryItemResponse> inventoryItems;
}
