package com.biobac.warehouse.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiveIngredient extends BaseAuditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Ingredient ingredient;

    @ManyToOne
    private Warehouse warehouse;

    private Long companyId;

    private LocalDateTime importDate;

    private LocalDateTime expirationDate;

    private LocalDateTime manufacturingDate;

    private BigDecimal price;

    private BigDecimal lastPrice;

    private Double quantity;

    private Double receivedQuantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private ReceiveGroup group;

    @ManyToOne
    private ReceiveIngredientStatus status;

    private boolean deleted = false;

    @OneToOne(mappedBy = "receiveIngredient")
    private IngredientDetail detail;

    @OneToMany(mappedBy = "receiveIngredient")
    private List<ReceiveExpense> expenses;
}
