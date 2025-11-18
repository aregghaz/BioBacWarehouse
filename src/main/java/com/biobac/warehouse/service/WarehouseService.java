
package com.biobac.warehouse.service;


import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.dto.WarehouseDto;
import com.biobac.warehouse.entity.Warehouse;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.request.WarehouseRequest;
import com.biobac.warehouse.response.WarehouseResponse;
import org.springframework.data.util.Pair;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface WarehouseService {
    Pair<List<WarehouseResponse>, PaginationMetadata> getPagination(
            Map<String, FilterCriteria> filters,
            Integer page,
            Integer size,
            String sortBy,
            String sortDir
    );

    WarehouseResponse getById(Long id);

    WarehouseResponse create(WarehouseRequest dto);

    Warehouse getWarehouseById(Long id);

    WarehouseResponse update(Long id, WarehouseRequest dto);

    void delete(Long id);

    List<WarehouseResponse> getAll();
}
