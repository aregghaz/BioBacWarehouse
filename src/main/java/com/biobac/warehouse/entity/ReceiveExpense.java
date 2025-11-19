package com.biobac.warehouse.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
public class ReceiveExpense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private ReceiveGroup group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receive_ingredient_id")
    private ReceiveIngredient receiveIngredient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_type_id", nullable = false)
    private ExpenseType expenseType;

    private BigDecimal amount;
}
