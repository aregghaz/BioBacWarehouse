package com.biobac.warehouse.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class ReceiveIngredientResponse extends AuditableResponse {
    private Long id;
    private Double quantity;
    private Double receivedQuantity;
    private String warehouseName;
    private Long warehouseId;
    private String ingredientName;
    private Long ingredientId;
    private String unitName;
    private List<UnitTypeCalculatedResponse> unitTypeConfigs;
    private List<UnitTypeCalculatedResponse> receivedUnitTypeConfigs;
    private LocalDate importDate;
    private LocalDate expirationDate;
    private LocalDate manufacturingDate;
    private Long companyId;
    private String companyName;
    private BigDecimal price;
    private BigDecimal lastPrice;
    private Long groupId;
    private String status;
    private Long statusId;
    private boolean succeed;
    private boolean deleted;
}
