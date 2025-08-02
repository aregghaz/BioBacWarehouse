
package com.biobac.warehouse.service;


import com.biobac.warehouse.dto.WarehouseDto;
import com.biobac.warehouse.entity.Warehouse;
import com.biobac.warehouse.mapper.WarehouseMapper;
import com.biobac.warehouse.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

public interface WarehouseService {

    @Transactional(readOnly = true)
    List<WarehouseDto> getAll();

    @Transactional(readOnly = true)
    WarehouseDto getById(Long id);

    @Transactional
    WarehouseDto create(WarehouseDto dto);

    @Transactional
    WarehouseDto update(Long id, WarehouseDto dto);

    @Transactional
    void delete(Long id);
}
