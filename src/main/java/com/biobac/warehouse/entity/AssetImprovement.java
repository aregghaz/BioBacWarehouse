package com.biobac.warehouse.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class AssetImprovement extends BaseAuditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Asset asset;

    private LocalDateTime date;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(columnDefinition = "text")
    private String comment;

    private Boolean extendLife = false;

    private Integer monthsExtended = 0;

    @ManyToOne
    private AssetAction action;
}
