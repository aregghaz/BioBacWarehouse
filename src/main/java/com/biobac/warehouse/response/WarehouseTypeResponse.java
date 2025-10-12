package com.biobac.warehouse.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WarehouseTypeResponse extends AuditableResponse {
    private Long id;
    private String type;
}
