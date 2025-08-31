package com.biobac.warehouse.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UnitCreateRequest {
    private String name;
    private List<Long> unitTypeIds;
}
