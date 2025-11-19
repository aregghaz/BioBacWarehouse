package com.biobac.warehouse.response;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AssetResponse extends AuditableResponse {
    private Long id;
    private String name;
    private String code;
    private BigDecimal originalCost;
    private BigDecimal currentCost;
    private BigDecimal accumulatedDepreciation;
    private Integer usefulLifeMonths;
    private String note;
    @JsonUnwrapped(prefix = "category")
    private EntityReferenceResponse category;
    @JsonUnwrapped(prefix = "depreciationMethod")
    private EntityReferenceResponse depreciationMethod;
    @JsonUnwrapped(prefix = "department")
    private EntityReferenceResponse department;
    @JsonUnwrapped(prefix = "warehouse")
    private EntityReferenceResponse warehouse;
}
