package com.biobac.warehouse.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "ingredient_history")
@Getter
@Setter
public class IngredientHistory extends BaseAuditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    private String action;

    private Double quantityBefore;

    private Double quantityAfter;

    private String notes;

    private Long companyId;

    private BigDecimal lastPrice;

    private Long userId;
}