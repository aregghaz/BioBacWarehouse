package com.biobac.warehouse.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class IngredientExpenseRequest {
    private Long expenseTypeId;
    private BigDecimal amount;
}
