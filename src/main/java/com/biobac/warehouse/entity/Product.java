package com.biobac.warehouse.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
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

    @OneToOne(mappedBy = "product")
    private RecipeItem recipeItem;

    @OneToMany(mappedBy = "product")
    private List<InventoryItem> inventoryItems = new ArrayList<>();

    private Long companyId;

    private Long unitId;
}