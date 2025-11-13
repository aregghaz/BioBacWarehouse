package com.biobac.warehouse.service;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.request.ManufactureCalculateRequest;
import com.biobac.warehouse.request.ManufactureProductRequest;
import com.biobac.warehouse.response.ManufactureCalculateResponse;
import com.biobac.warehouse.response.ManufactureProductResponse;
import org.springframework.data.util.Pair;

import java.util.List;
import java.util.Map;

public interface ManufactureProductService {

    ManufactureProductResponse createForProduct(ManufactureProductRequest request);

    Pair<List<ManufactureProductResponse>, PaginationMetadata> getByProductId(Map<String, FilterCriteria> filters,
                                                                              Integer page,
                                                                              Integer size,
                                                                              String sortBy,
                                                                              String sortDir);

    List<ManufactureCalculateResponse> calculateProductions(List<ManufactureCalculateRequest> request);
}
