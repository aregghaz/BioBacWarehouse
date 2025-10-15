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

    @ManyToOne(optional = false)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Column(nullable = false)
    private LocalDate period;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal amount;

    private Boolean paused = false;

    private Boolean manual = false;
}
