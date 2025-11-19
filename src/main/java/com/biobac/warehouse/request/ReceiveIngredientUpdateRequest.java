package com.biobac.warehouse.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class ReceiveIngredientUpdateRequest {
    private Long id;
    private Long warehouseId;
    private Long ingredientId;
    private Long companyId;
    private BigDecimal price;
    private LocalDateTime importDate;
    private LocalDateTime manufacturingDate;
    private Double quantity;
    private Double receivedQuantity;
    private Long statusId;
}
