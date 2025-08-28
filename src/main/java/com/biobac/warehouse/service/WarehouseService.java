
package com.biobac.warehouse.service;


import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.dto.WarehouseDto;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.WarehouseResponse;
import org.springframework.data.util.Pair;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface WarehouseService {

    @Transactional(readOnly = true)
    Pair<List<WarehouseResponse>, PaginationMetadata> getPagination(Map<String, FilterCriteria> filters,
                                                                    Integer page,
                                                                    Integer size,
                                                                    String sortBy,
                                                                    String sortDir);

    @Transactional(readOnly = true)
    WarehouseResponse getById(Long id);

    @Transactional
    WarehouseResponse create(WarehouseDto dto);

    @Transactional
    WarehouseResponse update(Long id, WarehouseDto dto);

    @Transactional
    void delete(Long id);

    @Transactional(readOnly = true)
    List<WarehouseResponse> getAll();
}
