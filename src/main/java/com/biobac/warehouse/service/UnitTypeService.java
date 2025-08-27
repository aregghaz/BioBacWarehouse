package com.biobac.warehouse.service;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.dto.UnitTypeDto;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.request.UnitTypeCreateRequest;
import org.springframework.data.util.Pair;

import java.util.List;
import java.util.Map;

public interface UnitTypeService {
    UnitTypeDto create(UnitTypeCreateRequest request);

    UnitTypeDto getById(Long id);

    UnitTypeDto update(Long id, UnitTypeCreateRequest request);

    void delete(Long id);

    List<UnitTypeDto> getAll();

    Pair<List<UnitTypeDto>, PaginationMetadata> pagination(Map<String, FilterCriteria> filters,
                                                           Integer page,
                                                           Integer size,
                                                           String sortBy,
                                                           String sortDir);
}
