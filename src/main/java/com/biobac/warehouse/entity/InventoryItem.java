package com.biobac.warehouse.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


import java.time.LocalDate;

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
    private Ingredient ingredient;
    
    @ManyToOne
    private IngredientGroup ingredientGroup;
    
    @Column(name = "group_id")
    private Long groupId;

    @ManyToOne
    private Warehouse warehouse;
    
    @Column(name = "warehouse_id")
    private Long warehouseId;

    private Integer quantity;
    private LocalDate lastUpdated;
}
