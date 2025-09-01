package com.biobac.warehouse.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UnitTypeConfigResponse {
    private Long id;
    private Long unitTypeId;
    private String unitTypeName;
    private Double size;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
