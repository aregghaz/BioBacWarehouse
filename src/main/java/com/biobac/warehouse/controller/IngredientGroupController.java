package com.biobac.warehouse.controller;

import com.biobac.warehouse.dto.IngredientGroupDto;
import com.biobac.warehouse.service.IngredientGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ingredient-groups")
@RequiredArgsConstructor
public class IngredientGroupController {

    private final IngredientGroupService service;

    @GetMapping
    public List<IngredientGroupDto> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public IngredientGroupDto getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    public IngredientGroupDto create(@RequestBody IngredientGroupDto dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    public IngredientGroupDto update(@PathVariable Long id, @RequestBody IngredientGroupDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}