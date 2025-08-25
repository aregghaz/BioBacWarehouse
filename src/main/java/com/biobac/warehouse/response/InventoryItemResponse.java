package com.biobac.warehouse.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class InventoryItemResponse {
    private Long id;
    private Double quantity;
    private Long warehouseId;
    private LocalDateTime lastUpdated;
}
