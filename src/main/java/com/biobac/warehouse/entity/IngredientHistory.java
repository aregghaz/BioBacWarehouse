package com.biobac.warehouse.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    @ManyToOne
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    private boolean increase;

    private Double quantityResult;

    private Double quantityChange;

    private String notes;

    private Long companyId;

    private BigDecimal lastPrice;

    private Long userId;

    private LocalDateTime timestamp;

    @ManyToOne
    private HistoryAction action;
}