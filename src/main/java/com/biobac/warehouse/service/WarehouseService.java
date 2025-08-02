
package com.biobac.warehouse.service;


import com.biobac.warehouse.dto.WarehouseDto;
import com.biobac.warehouse.entity.Warehouse;
import com.biobac.warehouse.mapper.WarehouseMapper;
import com.biobac.warehouse.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WarehouseService {
    private final WarehouseRepository repo;
    private final WarehouseMapper mapper;

    public List<WarehouseDto> getAll() {
        return repo.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public WarehouseDto getById(Long id) {
        return mapper.toDto(repo.findById(id).orElseThrow());
    }

    public WarehouseDto create(WarehouseDto dto) {
        return mapper.toDto(repo.save(mapper.toEntity(dto)));
    }

    public WarehouseDto update(Long id, WarehouseDto dto) {
        Warehouse existing = repo.findById(id).orElseThrow();
        existing.setName(dto.getName());
        existing.setLocation(dto.getLocation());
        existing.setType(dto.getType());
        return mapper.toDto(repo.save(existing));
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}
