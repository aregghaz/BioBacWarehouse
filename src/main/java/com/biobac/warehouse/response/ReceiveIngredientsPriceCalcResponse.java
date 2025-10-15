package com.biobac.warehouse.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class ReceiveIngredientsPriceCalcResponse {
    private BigDecimal totalPrice;
    private List<Ingredients> ingredients;
    private List<Expenses> expenses;

    @Getter
    @Setter
    public static class Ingredients {
        private Long ingredientId;
        private String ingredientName;
        private Double quantity;
        private BigDecimal price;
        private BigDecimal calculatedPrice;
        private BigDecimal total;
    }
    
    @Getter
    @Setter
    public static class Expenses {
        private String expenseTypeName;
        private BigDecimal amount;
    }
}
