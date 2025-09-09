package com.biobac.warehouse.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "attribute_value",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"definition_id", "product_id"}),
                @UniqueConstraint(columnNames = {"definition_id", "ingredient_id"}),
                @UniqueConstraint(columnNames = {"definition_id", "warehouse_id"})
        })
public class AttributeValue extends BaseAuditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "definition_id")
    private AttributeDefinition definition;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttributeTargetType targetType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id")
    private Ingredient ingredient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    @Column(name = "value", length = 2048)
    private String value;

    @Column(name = "deleted")
    private boolean deleted = false;
}
