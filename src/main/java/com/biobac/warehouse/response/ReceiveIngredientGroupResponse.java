package com.biobac.warehouse.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ReceiveIngredientGroupResponse {
    private Long groupId;
    private List<ReceiveIngredientResponse> items;
    private List<ReceiveExpenseResponse> expenses;
}
