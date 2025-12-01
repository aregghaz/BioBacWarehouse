package com.biobac.warehouse.response;

import com.biobac.warehouse.entity.AssetAction;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class AssetImprovementResponse extends AuditableResponse{
    private Long id;
    private Long assetId;
    private LocalDateTime date;
    private BigDecimal amount;
    private String comment;
    private Boolean extendLife;
    private Integer monthsExtended;
    private AssetAction action;
}
