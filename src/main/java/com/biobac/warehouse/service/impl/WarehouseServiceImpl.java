
package com.biobac.warehouse.service.impl;


import com.biobac.warehouse.dto.WarehouseDto;
import com.biobac.warehouse.entity.Warehouse;
import com.biobac.warehouse.mapper.WarehouseMapper;
import com.biobac.warehouse.repository.WarehouseRepository;
import com.biobac.warehouse.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {
    private final WarehouseRepository repo;
    private final WarehouseMapper mapper;

    @Transactional(readOnly = true)
    @Override
    public List<WarehouseDto> getAll() {
        return repo.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    @Override
    public WarehouseDto getById(Long id) {
        return mapper.toDto(repo.findById(id).orElseThrow());
    }
    @Transactional
    @Override
    public WarehouseDto create(WarehouseDto dto) {
        return mapper.toDto(repo.save(mapper.toEntity(dto)));
    }
    @Transactional
    @Override
    public WarehouseDto update(Long id, WarehouseDto dto) {
        Warehouse existing = repo.findById(id).orElseThrow();
        existing.setName(dto.getName());
        existing.setLocation(dto.getLocation());
        existing.setType(dto.getType());
        return mapper.toDto(repo.save(existing));
    }
    @Transactional
    @Override
    public void delete(Long id) {
        repo.deleteById(id);
    }
}
