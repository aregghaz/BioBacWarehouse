package com.biobac.warehouse.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;                          // Asset name or description
    private LocalDate startDate;                  // Date of commissioning
    private BigDecimal originalCost;              // Initial acquisition cost
    private BigDecimal currentCost;               // Current cost after improvements
    private BigDecimal accumulatedDepreciation;   // Total depreciation applied so far
    private BigDecimal residualValue;             // Remaining value (auto-calculated)
    private Integer usefulLifeMonths;             // Total useful life in months

    @ManyToOne
    private AssetCategory category;

    @ManyToOne
    private DepreciationMethod depreciationMethod;

    @ManyToOne
    private AssetStatus status;

    @ManyToOne
    private Department department;

    @ManyToOne
    private Warehouse warehouse;

    private String note;

    public void recalcResidual() {
        if (currentCost == null) currentCost = BigDecimal.ZERO;
        if (accumulatedDepreciation == null) accumulatedDepreciation = BigDecimal.ZERO;
        this.residualValue = currentCost.subtract(accumulatedDepreciation);
    }
}
