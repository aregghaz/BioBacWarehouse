package com.biobac.warehouse.service;

import com.biobac.warehouse.dto.ChangeComponentDto;
import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.dto.TransferComponentDto;
import com.biobac.warehouse.entity.ComponentType;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.ComponentBalanceQuantityResponse;
import com.biobac.warehouse.response.InventoryResponse;
import com.biobac.warehouse.response.ProductGroupResponse;
import org.springframework.data.util.Pair;

import java.util.List;
import java.util.Map;

public interface InventoryService {
    void changeProduct(List<ChangeComponentDto> componentDtoList);

    void changeIngredient(List<ChangeComponentDto> componentDtoList);

    Pair<List<InventoryResponse>, PaginationMetadata> getPagination(Map<String, FilterCriteria> filters,
                                                                    Integer page,
                                                                    Integer size,
                                                                    String sortBy,
                                                                    String sortDir,
                                                                    ComponentType type);
}
