package com.biobac.warehouse.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
public class ManufactureProduct extends BaseAuditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Product product;

    @ManyToOne
    private Warehouse warehouse;

    private LocalDate expirationDate;

    private LocalDate manufacturingDate;

    private Double quantity;

    private BigDecimal price;

    @OneToMany(mappedBy = "manufactureProduct")
    private List<ManufactureComponent> manufactureComponents;
}
