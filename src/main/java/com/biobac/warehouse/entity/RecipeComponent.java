package com.biobac.warehouse.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "recipe_components")
@Getter
@Setter
public class RecipeComponent extends BaseAuditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "recipe_item_id", nullable = false)
    private RecipeItem recipeItem;

    @ManyToOne(optional = true)
    @JoinColumn(name = "ingredient_id", nullable = true)
    private Ingredient ingredient;

    @ManyToOne(optional = true)
    @JoinColumn(name = "product_id", nullable = true)
    private Product product;

    @Column(nullable = false)
    private Double quantity;
}
