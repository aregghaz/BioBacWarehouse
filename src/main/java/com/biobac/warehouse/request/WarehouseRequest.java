package com.biobac.warehouse.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class WarehouseRequest {
    private String name;
    private String location;
    private List<Long> typeIds;
    private List<Long> attributeGroupIds;
    private Long warehouseGroupId;
    private List<AttributeUpsertRequest> attributes;
}
