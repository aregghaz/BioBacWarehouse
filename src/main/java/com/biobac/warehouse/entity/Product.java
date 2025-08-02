package com.biobac.warehouse.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private String sku;

    @ManyToMany
    @JoinTable(name = "product_ingredient",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "ingredient_id"))
    private List<Ingredient> ingredients;
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecipeItem> recipeItems;
    
    @OneToMany(mappedBy = "product")
    private List<InventoryItem> inventoryItems;
    
    @ManyToOne
    @JoinColumn(name = "parent_product_id")
    private Product parentProduct;
    
    @OneToMany(mappedBy = "parentProduct")
    private List<Product> childProducts;

}
