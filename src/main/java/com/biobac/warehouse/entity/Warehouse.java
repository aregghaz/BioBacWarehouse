package com.biobac.warehouse.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
public class Warehouse extends BaseAuditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String location;

    private List<Long> attributeGroupIds;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_group_id")
    private WarehouseGroup warehouseGroup;

    @ManyToMany
    @JoinTable(
            name = "warehouse_types_mapping",
            joinColumns = @JoinColumn(name = "warehouse_id"),
            inverseJoinColumns = @JoinColumn(name = "type_id")
    )
    private List<WarehouseType> types;
}
