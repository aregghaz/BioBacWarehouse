package com.biobac.warehouse.service;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.dto.UnitDto;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.request.UnitCreateRequest;
import org.springframework.data.util.Pair;

import java.util.List;
import java.util.Map;

public interface UnitService {
    UnitDto getById(Long id);

    UnitDto create(UnitCreateRequest request);

    UnitDto update(Long id, UnitCreateRequest request);

    void delete(Long id);

    List<UnitDto> getAll();

    Pair<List<UnitDto>, PaginationMetadata> pagination(Map<String, FilterCriteria> filters,
                                                       Integer page,
                                                       Integer size,
                                                       String sortBy,
                                                       String sortDir);
}
