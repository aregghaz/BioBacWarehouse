package com.biobac.warehouse.controller;


import com.biobac.warehouse.dto.IngredientDto;
import com.biobac.warehouse.service.IngredientService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ingredients")
@RequiredArgsConstructor
public class IngredientController {

    private final IngredientService service;

    @GetMapping
    public List<IngredientDto> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public IngredientDto getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    public IngredientDto create(@RequestBody IngredientDto dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    public IngredientDto update(@PathVariable Long id, @RequestBody IngredientDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
