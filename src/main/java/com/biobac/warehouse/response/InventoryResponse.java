package com.biobac.warehouse.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class InventoryResponse {
    private LocalDateTime date;
    private String warehouseName;
    private Double quantity;
    private String componentName;
}
