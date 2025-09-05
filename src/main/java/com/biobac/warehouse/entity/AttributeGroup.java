package com.biobac.warehouse.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
public class AttributeGroup extends BaseAuditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 2048)
    private String description;

    @Column(name = "deleted")
    private boolean deleted = false;

    @ManyToMany(mappedBy = "groups")
    private Set<AttributeDefinition> definitions = new HashSet<>();
}
