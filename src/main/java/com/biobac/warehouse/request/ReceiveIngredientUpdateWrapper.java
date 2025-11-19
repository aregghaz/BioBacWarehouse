package com.biobac.warehouse.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ReceiveIngredientUpdateWrapper {
    private List<ReceiveIngredientUpdateRequest> requests;
    private List<IngredientExpenseRequest> expenseRequests;
}
