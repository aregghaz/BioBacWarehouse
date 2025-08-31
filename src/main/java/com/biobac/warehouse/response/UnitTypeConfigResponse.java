package com.biobac.warehouse.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UnitTypeConfigResponse {
    private Long id;
    private Long unitTypeId;
    private String unitTypeName;
    private Double size;
}
