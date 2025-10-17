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
public class Asset extends BaseAuditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String code;

    private LocalDate commissioningDate;

    private LocalDate acquisitionDate;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal initialCost;

    private Integer usefulLifeMonths;

    @ManyToOne
    private AssetCategory category;

    @ManyToOne
    private DepreciationMethod depreciationMethod;

    @ManyToOne
    private AssetStatus status;

    @ManyToOne
    private Department department;

    private String responsible;

    @ManyToOne(fetch = FetchType.LAZY)
    private Warehouse warehouse;

    @Column(columnDefinition = "text")
    private String note;

    @Enumerated(EnumType.STRING)
    private AssetAcquisitionType acquisitionType = AssetAcquisitionType.REGISTERED; // REGISTERED or RECEIVED

    private Long receiptId;

    private Boolean depreciationPaused = false;

    @OneToMany(mappedBy = "asset")
    private List<DepreciationRecord> depreciationRecords;

    @OneToMany(mappedBy = "asset")
    private List<AssetImprovement> improvements;
}
