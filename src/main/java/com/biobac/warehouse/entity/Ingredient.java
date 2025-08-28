package com.biobac.warehouse.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Ingredient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private boolean active;

    @ManyToOne
    private IngredientGroup ingredientGroup;

    @OneToOne(mappedBy = "ingredient")
    private RecipeItem recipeItem;

    @OneToMany(mappedBy = "ingredient")
    private List<RecipeComponent> recipeComponents = new ArrayList<>();

    @OneToMany(mappedBy = "ingredient")
    private List<InventoryItem> inventoryItems = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id")
    private Unit unit;
}
