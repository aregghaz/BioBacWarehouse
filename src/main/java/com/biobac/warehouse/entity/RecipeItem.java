package com.biobac.warehouse.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class RecipeItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToOne
    @JoinColumn(name = "product_id", unique = true)
    private Product product;

    @OneToOne
    @JoinColumn(name = "ingredient_id", unique = true)
    private Ingredient ingredient;

    @OneToMany(mappedBy = "recipeItem")
    private List<RecipeComponent> components = new ArrayList<>();

    private String notes;
}