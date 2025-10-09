package com.biobac.warehouse.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class ComponentBalanceProdResponse {
    private Long id;
    private BigDecimal selfWorthPrice;
    private BigDecimal totalPrice;
    private String productName;
    private String productGroupName;
    private String warehouseName;
    private Double minimalBalance;
    private Double balance;
    private LocalDate expirationDate;
    private String unitName;
}
