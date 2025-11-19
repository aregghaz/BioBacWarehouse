package com.biobac.warehouse.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class ComponentBalanceIngResponse {
    private Long id;
    private Long ingredientId;
    private String ingredientName;
    private String ingredientGroupName;
    private String warehouseName;
    private BigDecimal selfWorthPrice;
    private BigDecimal totalPrice;
    private Double ingredientMinimalBalance;
    private Double balance;
    private LocalDateTime expirationDate;
    private String ingredientUnitName;
}