package com.biobac.warehouse.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Ingredient extends BaseAuditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    @ManyToOne
    private IngredientGroup ingredientGroup;

    private List<Long> attributeGroupIds;

    private BigDecimal price;

    @OneToMany(mappedBy = "ingredient")
    private List<InventoryItem> inventoryItems = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id")
    private Unit unit;

    @OneToMany(mappedBy = "ingredient", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IngredientUnitType> unitTypeConfigs = new ArrayList<>();

    private Integer expiration;

    @OneToMany(mappedBy = "ingredient")
    private List<IngredientHistory> histories = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_warehouse_id")
    private Warehouse defaultWarehouse;

    private boolean deleted = false;
}
