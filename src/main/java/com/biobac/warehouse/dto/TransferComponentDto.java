package com.biobac.warehouse.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TransferComponentDto {
    private Long fromWarehouseId;
    private Long toWarehouseId;
    private Long componentId;
    private Double quantity;
    private LocalDateTime date;
}
