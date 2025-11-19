package com.biobac.warehouse.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
public class IngredientBalance extends BaseAuditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ingredient_id")
    private Ingredient ingredient;

    private Double balance;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "ingredientBalance")
    private List<IngredientDetail> details;
}
