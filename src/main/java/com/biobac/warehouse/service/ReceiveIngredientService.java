package com.biobac.warehouse.service;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.request.*;
import com.biobac.warehouse.response.ReceiveIngredientGroupResponse;
import com.biobac.warehouse.response.ReceiveIngredientResponse;
import com.biobac.warehouse.response.ReceiveIngredientsPriceCalcResponse;
import com.biobac.warehouse.response.SelectResponse;
import org.springframework.data.util.Pair;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface ReceiveIngredientService {
    Pair<List<ReceiveIngredientResponse>, PaginationMetadata> getPagination(Map<String, FilterCriteria> filters,
                                                                            Integer page,
                                                                            Integer size,
                                                                            String sortBy,
                                                                            String sortDir);

    Pair<List<ReceiveIngredientResponse>, PaginationMetadata> getSucceeded(Map<String, FilterCriteria> filters,
                                                                           Integer page,
                                                                           Integer size,
                                                                           String sortBy,
                                                                           String sortDir);

    Pair<List<ReceiveIngredientResponse>, PaginationMetadata> getPending(Map<String, FilterCriteria> filters,
                                                                         Integer page,
                                                                         Integer size,
                                                                         String sortBy,
                                                                         String sortDir);

    ReceiveIngredientGroupResponse getByGroupId(Long groupId);

    List<ReceiveIngredientResponse> receive(List<ReceiveIngredientRequest> request, List<IngredientExpenseRequest> expenseRequests);

    List<ReceiveIngredientResponse> finalizeReceive(Long groupId, List<ReceiveIngredientFinalizeRequest> request);

    List<ReceiveIngredientResponse> update(Long groupId, List<ReceiveIngredientUpdateRequest> request, List<IngredientExpenseRequest> expenseRequests);

    void delete(Long groupId);

    ReceiveIngredientsPriceCalcResponse calcIngredientPrices(List<ReceiveIngredientsPriceCalcRequest> ingredients, List<IngredientExpenseRequest> expenses);

    List<SelectResponse> getStatusesSelect();
}
