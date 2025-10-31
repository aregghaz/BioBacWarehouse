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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "type_id", nullable = false)
    private WarehouseType warehouseType;

    private boolean deleted = false;
}
