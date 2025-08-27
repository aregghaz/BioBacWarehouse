package com.biobac.warehouse.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RecipeItemTableResponse {
    private Long id;
    private String name;
    private String notes;
    private List<RecipeItemResponse.RecipeComponentDto> components;
}
