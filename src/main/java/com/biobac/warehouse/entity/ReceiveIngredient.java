package com.biobac.warehouse.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
public class ReceiveIngredient extends BaseAuditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Ingredient ingredient;

    @ManyToOne
    private Warehouse warehouse;

    private Long companyId;

    private LocalDate importDate;

    private LocalDate expirationDate;

    private LocalDate manufacturingDate;

    private BigDecimal price;

    private Double quantity;
}
