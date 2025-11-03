package com.biobac.warehouse.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class IngredientDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ingredient_balance_id")
    private IngredientBalance ingredientBalance;

    private LocalDateTime importDate;

    private LocalDateTime expirationDate;

    private LocalDateTime manufacturingDate;

    private Double quantity;

    private BigDecimal price;

    @OneToOne
    @JoinColumn(name = "receive_ingredient_id", unique = true)
    private ReceiveIngredient receiveIngredient;
}
