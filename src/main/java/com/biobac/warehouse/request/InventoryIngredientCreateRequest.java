package com.biobac.warehouse.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class InventoryIngredientCreateRequest {
    @NotNull(message = "Warehouse ID is required")
    private Long warehouseId;

    @NotNull(message = "Ingredient ID is required")
    private Long ingredientId;

    @NotNull(message = "Company ID is required")
    private Long companyId;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "Import date is required")
    @PastOrPresent(message = "Import date cannot be in the future")
    private LocalDate importDate;

    @NotNull(message = "Manufacturing date is required")
    @PastOrPresent(message = "Manufacturing date cannot be in the future")
    private LocalDate manufacturingDate;

    @NotEmpty(message = "At least one unit type is required")
    private List<@Valid InventoryUnitTypeRequest> unitTypes;
}
