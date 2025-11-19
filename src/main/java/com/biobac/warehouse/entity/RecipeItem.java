package com.biobac.warehouse.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class RecipeItem extends BaseAuditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToMany(mappedBy = "recipeItem")
    private List<Product> products = new ArrayList<>();

    @OneToMany(mappedBy = "recipeItem")
    private List<RecipeComponent> components = new ArrayList<>();

    private String notes;
}