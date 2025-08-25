package com.biobac.warehouse.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RecipeItemCreateRequest {
    private String name;
    private String notes;
    private List<RecipeComponentRequest> components;
}
