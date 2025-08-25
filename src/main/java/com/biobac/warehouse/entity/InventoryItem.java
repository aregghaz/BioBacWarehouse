package com.biobac.warehouse.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class InventoryItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Product product;

    @ManyToOne
    private IngredientGroup ingredientGroup;

    @ManyToOne
    private Ingredient ingredient;

    @ManyToOne
    private Warehouse warehouse;

    private Double quantity;
    private LocalDateTime lastUpdated;
}
