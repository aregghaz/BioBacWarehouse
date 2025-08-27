package com.biobac.warehouse.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UnitCreateRequest {
    private String name;
    private Long unitTypeId;
}
