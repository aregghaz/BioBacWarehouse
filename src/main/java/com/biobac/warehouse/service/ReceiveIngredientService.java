package com.biobac.warehouse.service;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.request.IngredientExpenseRequest;
import com.biobac.warehouse.request.ReceiveIngredientRequest;
import com.biobac.warehouse.request.ReceiveIngredientUpdateRequest;
import com.biobac.warehouse.response.ReceiveIngredientGroupResponse;
import com.biobac.warehouse.response.ReceiveIngredientResponse;
import org.springframework.data.util.Pair;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface ReceiveIngredientService {

    @Transactional
    List<ReceiveIngredientResponse> receive(List<ReceiveIngredientRequest> request, List<IngredientExpenseRequest> expenseRequests);

    @Transactional(readOnly = true)
    Pair<List<ReceiveIngredientResponse>, PaginationMetadata> getByIngredientId(Long ingredientId, Map<String, FilterCriteria> filters,
                                                                                Integer page,
                                                                                Integer size,
                                                                                String sortBy,
                                                                                String sortDir);

    @Transactional(readOnly = true)
    ReceiveIngredientGroupResponse getByGroupId(Long groupId);

    @Transactional
    List<ReceiveIngredientResponse> update(Long groupId, List<ReceiveIngredientUpdateRequest> request, List<IngredientExpenseRequest> expenseRequests);

    @Transactional
    void delete(Long groupId);
}
