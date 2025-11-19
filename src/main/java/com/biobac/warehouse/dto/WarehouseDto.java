package com.biobac.warehouse.dto;

import lombok.Data;

import java.util.List;
import com.biobac.warehouse.request.AttributeUpsertRequest;

@Data
public class WarehouseDto {
    private Long id;
    private String name;
    private String location;
    private String type;
    private List<AttributeUpsertRequest> attributes;
}
