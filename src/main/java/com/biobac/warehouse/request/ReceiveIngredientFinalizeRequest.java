package com.biobac.warehouse.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ReceiveIngredientFinalizeRequest {
    private Long id;

    @NotNull(message = "Import date is required")
    @PastOrPresent(message = "Import date cannot be in the future")
    private LocalDate importDate;

    @NotNull(message = "Manufacturing date is required")
    @PastOrPresent(message = "Manufacturing date cannot be in the future")
    private LocalDate manufacturingDate;
}
