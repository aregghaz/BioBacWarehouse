package com.biobac.warehouse.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ComponentBalanceIngResponse {
    private String name;
    private String ingredientGroupName;
    private Double balance;
    private LocalDate expirationDate;
}
