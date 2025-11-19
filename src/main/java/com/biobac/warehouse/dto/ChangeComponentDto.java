package com.biobac.warehouse.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ChangeComponentDto {
    private Long warehouseId;
    private Long componentId;
    private Double quantity;
    private LocalDateTime date;
}
