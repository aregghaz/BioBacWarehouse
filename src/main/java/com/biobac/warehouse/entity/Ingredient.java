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
    private String unit;
    private boolean active;
    private Double quantity;

    @ManyToOne
    private IngredientGroup group;
    
    @OneToMany(mappedBy = "parentIngredient")
    private List<IngredientComponent> childIngredientComponents;
    
    @OneToMany(mappedBy = "ingredient")
    private List<RecipeItem> recipeItems;
    
    @OneToMany(mappedBy = "ingredient", cascade = CascadeType.ALL)
    private List<IngredientHistory> history = new ArrayList<>();

}
