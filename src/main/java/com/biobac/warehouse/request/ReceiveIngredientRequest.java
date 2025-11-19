package com.biobac.warehouse.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ReceiveIngredientRequest {
    @NotNull(message = "Warehouse is required")
    private Long warehouseId;

    @NotNull(message = "Ingredient is required")
    private Long ingredientId;

    @NotNull(message = "Company is required")
    private Long companyId;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @NotEmpty(message = "Quantity is required")
    private Double quantity;
}
