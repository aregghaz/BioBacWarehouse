package com.biobac.warehouse.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WarehouseTableResponse {
    private Long id;
    private String name;
    private String location;
    private String type;
}
