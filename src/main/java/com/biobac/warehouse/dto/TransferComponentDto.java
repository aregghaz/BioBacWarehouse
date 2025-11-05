package com.biobac.warehouse.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransferComponentDto {
    private Long fromWarehouseId;
    private Long toWarehouseId;
    private Long componentId;
    private String componentType;
    private Double quantity;
}
