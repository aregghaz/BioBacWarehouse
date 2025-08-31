package com.biobac.warehouse.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UnitDto {
    private Long id;
    private String name;
    private List<Long> unitTypeIds;
    private List<String> unitTypeNames;
}
