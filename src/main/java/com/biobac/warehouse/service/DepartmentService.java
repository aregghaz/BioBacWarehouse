package com.biobac.warehouse.service;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.request.DepartmentRequest;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.DepartmentResponse;
import org.springframework.data.util.Pair;

import java.util.List;
import java.util.Map;

public interface DepartmentService {
    DepartmentResponse create(DepartmentRequest request);

    DepartmentResponse update(Long id, DepartmentRequest request);

    void delete(Long id);

    List<DepartmentResponse> getAll();

    Pair<List<DepartmentResponse>, PaginationMetadata> getPagination(Map<String, FilterCriteria> filters, Integer page, Integer size, String sortBy, String sortDir);

    DepartmentResponse getById(Long id);
}
