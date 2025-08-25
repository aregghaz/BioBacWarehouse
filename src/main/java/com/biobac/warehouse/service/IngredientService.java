package com.biobac.warehouse.service;

import com.biobac.warehouse.request.IngredientCreateRequest;
import com.biobac.warehouse.response.IngredientResponse;

public interface IngredientService {
    IngredientResponse create(IngredientCreateRequest request);
}
