package com.biobac.warehouse.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ReceiveIngredientsPriceCalcWrapper {
    private List<ReceiveIngredientsPriceCalcRequest> ingredients;
    private List<IngredientExpenseRequest> expenses;
}
