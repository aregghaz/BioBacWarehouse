package com.biobac.warehouse.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class ReceiveIngredientResponse extends AuditableResponse {
    private Long id;
    private Double quantity;
    private String warehouseName;
    private Long warehouseId;
    private String ingredientName;
    private Long ingredientId;
    private String unitName;
    private LocalDate importDate;
    private LocalDate expirationDate;
    private LocalDate manufacturingDate;
    private Long companyId;
    private String companyName;
    private BigDecimal price;
}
