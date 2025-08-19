
package com.biobac.warehouse.service;


import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.dto.WarehouseDto;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.WarehouseTableResponse;
import org.springframework.data.util.Pair;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface WarehouseService {

    @Transactional(readOnly = true)
    Pair<List<WarehouseTableResponse>, PaginationMetadata> getAll(Map<String, FilterCriteria> filters,
                                                                  Integer page,
                                                                  Integer size,
                                                                  String sortBy,
                                                                  String sortDir);

    @Transactional(readOnly = true)
    WarehouseDto getById(Long id);

    @Transactional
    WarehouseDto create(WarehouseDto dto);

    @Transactional
    WarehouseDto update(Long id, WarehouseDto dto);

    @Transactional
    void delete(Long id);
}
