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
public class ReceiveIngredient extends BaseAuditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Ingredient ingredient;

    @ManyToOne
    private Warehouse warehouse;

    private Long companyId;

    private LocalDate importDate;

    private LocalDate expirationDate;

    private LocalDate manufacturingDate;

    private BigDecimal price;

    private BigDecimal lastPrice;

    private Double quantity;

    private Double receivedQuantity;

    private Long groupId;

    @ManyToOne
    private ReceiveIngredientStatus status;

    private boolean deleted = false;

    @OneToOne(mappedBy = "receiveIngredient")
    private IngredientDetail detail;

    @OneToMany(mappedBy = "receiveIngredient")
    private List<ReceiveExpense> expenses;
}
