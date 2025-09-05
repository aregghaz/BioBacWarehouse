package com.biobac.warehouse.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class WarehouseResponse {
    private Long id;
    private String name;
    private String location;
    private String warehouseGroupName;
    private Long warehouseGroupId;
    private String type;
    private List<AttributeDefResponse> attributes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
