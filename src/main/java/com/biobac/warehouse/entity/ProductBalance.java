package com.biobac.warehouse.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
public class ProductBalance extends BaseAuditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.EAGER  )
    @JoinColumn(name = "product_id")
    private Product product;

    private Double balance;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "productBalance")
    private List<ProductDetail> details;
}
