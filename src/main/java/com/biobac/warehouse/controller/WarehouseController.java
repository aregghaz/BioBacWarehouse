package com.biobac.warehouse.controller;

import com.biobac.warehouse.dto.WarehouseDto;
import com.biobac.warehouse.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
public class WarehouseController {
    private final WarehouseService service;

    @GetMapping
    public List<WarehouseDto> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public WarehouseDto getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    public WarehouseDto create(@RequestBody WarehouseDto dto) {
        dto.setId(null); // new entity
        return service.create(dto);
    }

    @PutMapping("/{id}")
    public WarehouseDto update(@PathVariable Long id, @RequestBody WarehouseDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
