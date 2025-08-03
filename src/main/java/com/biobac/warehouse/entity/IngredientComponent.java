package com.biobac.warehouse.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ingredient_components")
@Getter
@Setter
public class IngredientComponent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "parent_ingredient_id", nullable = false)
    private Ingredient parentIngredient;
    
    @ManyToOne
    @JoinColumn(name = "child_ingredient_id", nullable = false)
    private Ingredient childIngredient;
    
    @Column(nullable = true)
    private Double quantity;
    
    // Default constructor required by JPA
    public IngredientComponent() {
    }
    
    // Convenience constructor
    public IngredientComponent(Ingredient parentIngredient, Ingredient childIngredient, Double quantity) {
        this.parentIngredient = parentIngredient;
        this.childIngredient = childIngredient;
        this.quantity = quantity;
    }
}