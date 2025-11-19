package com.biobac.warehouse.service;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.dto.TransferComponentDto;
import com.biobac.warehouse.entity.ComponentType;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.TransferResponse;
import org.springframework.data.util.Pair;

import java.util.List;
import java.util.Map;

public interface TransferService {
    void transferProduct(List<TransferComponentDto> request);

    void transferIngredient(List<TransferComponentDto> request);

    Pair<List<TransferResponse>, PaginationMetadata> getPagination(Map<String, FilterCriteria> filters,
                                                                   Integer page,
                                                                   Integer size,
                                                                   String sortBy,
                                                                   String sortDir,
                                                                   ComponentType type);
}
