package com.biobac.warehouse.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AssetResponse {
    private Long id;
    private String name;
    private BigDecimal originalCost;
    private BigDecimal currentCost;
    private BigDecimal accumulatedDepreciation;
    private Integer usefulLifeMonths;
    private Long categoryId;
    private String categoryName;
    private Long depreciationMethodId;
    private String depreciationMethodName;
    private Long departmentId;
    private String departmentName;
    private Long warehouseId;
    private String warehouseName;
    private String note;
}
