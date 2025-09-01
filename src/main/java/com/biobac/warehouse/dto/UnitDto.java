package com.biobac.warehouse.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class UnitDto {
    private Long id;
    private String name;
    private List<UnitTypeDto> unitTypes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
