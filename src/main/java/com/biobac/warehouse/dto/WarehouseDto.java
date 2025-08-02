package com.biobac.warehouse.dto;

import lombok.Data;

@Data
public class WarehouseDto {
    private Long id;
    private String name;
    private String location;
    private String type;
}
