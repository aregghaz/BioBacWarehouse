package com.biobac.warehouse.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "component_balance",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_balance_wh_ing", columnNames = {"warehouse_id", "ingredient_id"}),
                @UniqueConstraint(name = "uk_balance_wh_prod", columnNames = {"warehouse_id", "product_id"})
        })
@Getter
@Setter
public class ComponentBalance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id")
    private Ingredient ingredient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private Double balance;
}
