package com.biobac.warehouse.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class ReceiveIngredientUpdateRequest {
    private Long id;
    private Long warehouseId;
    private Long ingredientId;
    private Long companyId;
    private BigDecimal price;
    private LocalDate importDate;
    private LocalDate manufacturingDate;
    private Double quantity;
}
