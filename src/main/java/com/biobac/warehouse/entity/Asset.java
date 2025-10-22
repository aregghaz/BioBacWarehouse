package com.biobac.warehouse.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
public class Asset extends BaseAuditable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private LocalDate startDate;
    private BigDecimal originalCost;
    private BigDecimal currentCost;
    private BigDecimal accumulatedDepreciation;
    private BigDecimal residualValue;
    private Integer usefulLifeMonths;             

    @Column(unique = true)
    private String code;

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
