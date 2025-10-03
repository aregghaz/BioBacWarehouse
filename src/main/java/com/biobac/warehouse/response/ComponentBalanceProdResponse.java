package com.biobac.warehouse.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ComponentBalanceProdResponse {
    private String name;
    private String productGroupName;
    private Double balance;
    private LocalDate expirationDate;
}
