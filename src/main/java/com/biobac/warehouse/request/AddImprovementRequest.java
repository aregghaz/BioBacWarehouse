package com.biobac.warehouse.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class AddImprovementRequest {
    private LocalDate date;
    private BigDecimal amount;
    private String comment;
    private Boolean extendLife;
    private Integer monthsExtended;
}
