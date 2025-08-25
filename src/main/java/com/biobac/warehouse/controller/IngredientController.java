package com.biobac.warehouse.controller;

import com.biobac.warehouse.request.IngredientCreateRequest;
import com.biobac.warehouse.response.ApiResponse;
import com.biobac.warehouse.response.IngredientResponse;
import com.biobac.warehouse.service.IngredientService;
import com.biobac.warehouse.utils.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ingredient")
@RequiredArgsConstructor
public class IngredientController {
    private final IngredientService ingredientService;

    @PostMapping
    public ApiResponse<IngredientResponse> create(@RequestBody IngredientCreateRequest request) {
        IngredientResponse ingredientResponse = ingredientService.create(request);

        return ResponseUtil.success("Ingredient created successfully", ingredientResponse);
    }
}
