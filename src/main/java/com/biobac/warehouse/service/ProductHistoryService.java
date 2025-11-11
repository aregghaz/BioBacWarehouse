package com.biobac.warehouse.service;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.dto.ProductHistoryDto;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.ProductHistoryResponse;
import com.biobac.warehouse.response.ProductHistorySingleResponse;
import org.springframework.data.util.Pair;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ProductHistoryService {

    ProductHistorySingleResponse recordQuantityChange(ProductHistoryDto dto);

    Pair<List<ProductHistorySingleResponse>, PaginationMetadata> getHistoryForProduct(Long productId, Map<String, FilterCriteria> filters,
                                                                                      Integer page, Integer size, String sortBy, String sortDir);

    Pair<List<ProductHistorySingleResponse>, PaginationMetadata> getHistory(Map<String, FilterCriteria> filters,
                                                                            Integer page, Integer size, String sortBy, String sortDir);

    Pair<List<ProductHistoryResponse>, PaginationMetadata> getAll(Map<String, FilterCriteria> filters,
                                                                  Integer page, Integer size, String sortBy, String sortDir);

    Double getTotalForProduct(Long productId);

    Double getInitialForProduct(Long productId, LocalDateTime start);

    Double getEventualForProduct(Long productId, LocalDateTime end);

    Double getEventualForProduct(Long productId, Long warehouseId, LocalDateTime end);

    Double getSumOfIncreasedCount(Long id, LocalDateTime start, LocalDateTime end);

    Double getSumOfDecreasedCount(Long id, LocalDateTime start, LocalDateTime end);
}