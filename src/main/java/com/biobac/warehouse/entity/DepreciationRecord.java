package com.biobac.warehouse.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"asset_id", "period"})
})
@Getter
@Setter
public class DepreciationRecord extends BaseAuditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Asset asset;              // Which asset this record belongs to

    private LocalDate period;         // Month of depreciation (e.g., 2025-10-01)
    private BigDecimal amount;        // Depreciation amount for that month
    private LocalDate createdAt;      // Date of posting
}
