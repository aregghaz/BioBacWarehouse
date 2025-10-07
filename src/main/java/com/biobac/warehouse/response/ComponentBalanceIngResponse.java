package com.biobac.warehouse.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class ComponentBalanceIngResponse {
    private Long id;
    private String ingredientName;
    private String ingredientGroupName;
    private String warehouseName;
    private BigDecimal selfWorthPrice;
    private BigDecimal totalPrice;
    private Double minimalBalance;
    private Double balance;
    private LocalDate expirationDate;
}