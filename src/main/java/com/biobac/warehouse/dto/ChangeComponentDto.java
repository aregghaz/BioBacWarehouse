package com.biobac.warehouse.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeComponentDto {
    private Long warehouseId;
    private Long componentId;
    private String componentType;
    private Double quantity;
}
