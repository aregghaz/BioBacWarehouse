package com.biobac.warehouse.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExtraInventorySelection extends ComponentInventorySelection {
    @NotNull(message = "Quantity is required")
    private Double quantity;
}
