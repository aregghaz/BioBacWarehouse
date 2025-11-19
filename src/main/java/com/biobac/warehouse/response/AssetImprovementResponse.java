package com.biobac.warehouse.response;

import com.biobac.warehouse.entity.AssetAction;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class AssetImprovementResponse extends AuditableResponse{
    private Long id;
    private Long assetId;
    private LocalDate date;
    private BigDecimal amount;
    private String comment;
    private Boolean extendLife;
    private Integer monthsExtended;
    private AssetAction action;
}
