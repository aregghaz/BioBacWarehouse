package com.biobac.warehouse.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ComponentBalanceProdResponse {
    private String productName;
    private String productGroupName;
    private String warehouseName;
    private Double minimalBalance;
    private Double balance;
    private LocalDate expirationDate;
}
