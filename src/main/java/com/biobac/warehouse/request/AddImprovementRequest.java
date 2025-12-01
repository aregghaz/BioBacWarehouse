package com.biobac.warehouse.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class AddImprovementRequest {
    private LocalDateTime date;
    private BigDecimal amount;
    private String comment;
    private Boolean extendLife;
    private Integer monthsExtended;
    private Long actionId;
}
