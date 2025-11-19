package com.biobac.warehouse.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    private LocalDateTime expirationDate;

    private LocalDateTime manufacturingDate;

    private Double quantity;

    private BigDecimal price;

    @OneToMany(mappedBy = "manufactureProduct")
    private List<ManufactureComponent> manufactureComponents;
}
