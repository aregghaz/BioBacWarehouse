package com.biobac.warehouse.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
public class ProductDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_balance_id")
    private ProductBalance productBalance;

    private LocalDate expirationDate;

    private LocalDate manufacturingDate;

    private Double quantity;

    private BigDecimal price;
}
