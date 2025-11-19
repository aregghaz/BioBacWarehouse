package com.biobac.warehouse.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AssetRegisterRequest {
    private String name;
    private String code;
    private BigDecimal originalCost;
    private BigDecimal accumulatedDepreciation;
    private Integer usefulLifeMonths;
    private Long categoryId;
    private Long depreciationMethodId;
    private Long departmentId;
    private Long warehouseId;
    private String note;
}
