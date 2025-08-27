package com.biobac.warehouse.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "recipe_components")
@Getter
@Setter
public class RecipeComponent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "recipe_item_id", nullable = false)
    private RecipeItem recipeItem;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @Column(nullable = false)
    private Double quantity;

    private Long unitId;
}
