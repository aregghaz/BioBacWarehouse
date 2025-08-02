package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.dto.IngredientGroupDto;
import com.biobac.warehouse.entity.IngredientGroup;
import com.biobac.warehouse.mapper.IngredientGroupMapper;
import com.biobac.warehouse.repository.IngredientGroupRepository;
import com.biobac.warehouse.service.IngredientGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IngredientGroupServiceImpl implements IngredientGroupService {
    private final IngredientGroupRepository repository;
    private final IngredientGroupMapper mapper;

    @Transactional(readOnly = true)
    @Override
    public List<IngredientGroupDto> getAll() {
        return repository.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public IngredientGroupDto getById(Long id) {
        return mapper.toDto(repository.findById(id).orElseThrow());
    }

    @Transactional
    @Override
    public IngredientGroupDto create(IngredientGroupDto dto) {
        IngredientGroup entity = mapper.toEntity(dto);
        return mapper.toDto(repository.save(entity));
    }

    @Transactional
    @Override
    public IngredientGroupDto update(Long id, IngredientGroupDto dto) {
        IngredientGroup existing = repository.findById(id).orElseThrow();
        existing.setName(dto.getName());
        return mapper.toDto(repository.save(existing));
    }

    @Transactional
    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }
}