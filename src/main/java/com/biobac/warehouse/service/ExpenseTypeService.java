package com.biobac.warehouse.service;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.request.ExpenseTypeCreateRequest;
import com.biobac.warehouse.request.ExpenseTypeUpdateRequest;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.ExpenseTypeResponse;
import org.springframework.data.util.Pair;

import java.util.List;
import java.util.Map;

public interface ExpenseTypeService {
    ExpenseTypeResponse getById(Long id);

    ExpenseTypeResponse create(ExpenseTypeCreateRequest request);

    ExpenseTypeResponse update(ExpenseTypeUpdateRequest request);

    List<ExpenseTypeResponse> getAll();

    Pair<List<ExpenseTypeResponse>, PaginationMetadata> getPagination(Map<String, FilterCriteria> filters, Integer page, Integer size, String sortBy, String sortDir);

    void delete(Long id);
}
