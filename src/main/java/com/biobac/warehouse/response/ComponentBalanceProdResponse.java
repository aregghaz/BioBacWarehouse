package com.biobac.warehouse.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class ComponentBalanceProdResponse {
    private Long id;
    private BigDecimal selfWorthPrice;
    private BigDecimal totalPrice;
    private String productName;
    private String productGroupName;
    private String warehouseName;
    private Double productMinimalBalance;
    private Double balance;
    private LocalDateTime productExpirationDate;
    private String productUnitName;
}
