package com.biobac.warehouse.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ReceiveExpenseResponse {
    private Long id;
    private Long expenseTypeId;
    private String expenseTypeName;
    private BigDecimal amount;
}
