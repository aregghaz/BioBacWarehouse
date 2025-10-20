package com.biobac.warehouse.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class AssetImprovementResponse {
    private Long id;
    private Long assetId;
    private LocalDate date;
    private BigDecimal amount;
    private String comment;
    private Boolean extendLife;
    private Integer monthsExtended;
}
