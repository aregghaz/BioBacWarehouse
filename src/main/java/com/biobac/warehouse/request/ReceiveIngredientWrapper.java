package com.biobac.warehouse.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ReceiveIngredientWrapper {
    private List<ReceiveIngredientRequest> requests;
    private List<IngredientExpenseRequest> expenseRequests;
}
