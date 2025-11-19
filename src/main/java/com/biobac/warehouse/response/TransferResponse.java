package com.biobac.warehouse.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TransferResponse {
    private LocalDateTime date;
    private String fromWarehouseName;
    private String toWarehouseName;
    private Double quantity;
    private String componentName;
}
