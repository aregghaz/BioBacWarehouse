package com.biobac.warehouse.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Transfer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ComponentType type;

    private Long componentId;

    @ManyToOne
    private Warehouse from;

    @ManyToOne
    private Warehouse to;

    private LocalDateTime date;

    private Double quantity;
}
