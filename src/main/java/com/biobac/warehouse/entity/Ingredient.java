package com.biobac.warehouse.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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
    private String unit;
    private boolean active;

    @ManyToOne
    private IngredientGroup group;
    
    @ManyToOne
    @JoinColumn(name = "parent_ingredient_id")
    private Ingredient parentIngredient;
    
    @OneToMany(mappedBy = "parentIngredient")
    private List<Ingredient> childIngredients;
    
    @OneToMany(mappedBy = "ingredient")
    private List<RecipeItem> recipeItems;

}
