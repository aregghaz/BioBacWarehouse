package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.client.CompanyClient;
import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.entity.*;
import com.biobac.warehouse.exception.InvalidDataException;
import com.biobac.warehouse.exception.NotFoundException;
import com.biobac.warehouse.mapper.ReceiveIngredientMapper;
import com.biobac.warehouse.repository.*;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.request.IngredientExpenseRequest;
import com.biobac.warehouse.request.ReceiveIngredientRequest;
import com.biobac.warehouse.response.ApiResponse;
import com.biobac.warehouse.response.ReceiveIngredientResponse;
import com.biobac.warehouse.service.IngredientHistoryService;
import com.biobac.warehouse.service.ReceiveIngredientService;
import com.biobac.warehouse.utils.specifications.ReceiveIngredientSpecification;
import jakarta.persistence.criteria.JoinType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReceiveIngredientServiceImpl implements ReceiveIngredientService {
    private final IngredientRepository ingredientRepository;
    private final ReceiveIngredientRepository receiveIngredientRepository;
    private final WarehouseRepository warehouseRepository;
    private final ReceiveIngredientMapper receiveIngredientMapper;
    private final IngredientHistoryService ingredientHistoryService;
    private final CompanyClient companyClient;
    private final IngredientBalanceRepository ingredientBalanceRepository;
    private final IngredientDetailRepository ingredientDetailRepository;
    private final ExpenseTypeRepository expenseTypeRepository;
    private final ReceiveExpenseRepository receiveExpenseRepository;

    @Override
    @Transactional
    public List<ReceiveIngredientResponse> createForIngredient(
            List<ReceiveIngredientRequest> requests,
            List<IngredientExpenseRequest> expenseRequests) {

        BigDecimal additionalExpense = expenseRequests.stream()
                .map(IngredientExpenseRequest::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal receivedExpense = requests.stream()
                .map(r -> r.getPrice().multiply(BigDecimal.valueOf(r.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<ReceiveIngredientResponse> responses = new ArrayList<>();

        for (ReceiveIngredientRequest req : requests) {
            ReceiveIngredient receiveIngredient =
                    createSingleInventoryItemIngredient(req, additionalExpense, receivedExpense);

            for (IngredientExpenseRequest expReq : expenseRequests) {
                ExpenseType expenseType = expenseTypeRepository.findById(expReq.getExpenseTypeId())
                        .orElseThrow(() -> new NotFoundException("Expense type not found"));

                ReceiveExpense receiveExpense = new ReceiveExpense();
                receiveExpense.setReceiveIngredient(receiveIngredient);
                receiveExpense.setExpenseType(expenseType);
                receiveExpense.setAmount(expReq.getAmount());

                receiveExpenseRepository.save(receiveExpense);
            }

            responses.add(toReceiveIngredientResponse(receiveIngredient));
        }

        return responses;
    }

    private ReceiveIngredient createSingleInventoryItemIngredient(
            ReceiveIngredientRequest request,
            BigDecimal additionalExpense,
            BigDecimal receivedExpense) {

        Ingredient ingredient = ingredientRepository.findById(request.getIngredientId())
                .orElseThrow(() -> new NotFoundException("Ingredient not found"));

        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new NotFoundException("Warehouse not found"));

        double totalCount = request.getQuantity();

        BigDecimal ingredientTotal = request.getPrice().multiply(BigDecimal.valueOf(totalCount));

        BigDecimal proportionalExpense = BigDecimal.ZERO;
        if (receivedExpense.compareTo(BigDecimal.ZERO) > 0) {
            proportionalExpense = ingredientTotal
                    .divide(receivedExpense, 3, RoundingMode.HALF_UP)
                    .multiply(additionalExpense);
        }

        BigDecimal selfWorthPrice = request.getPrice().add(
                proportionalExpense.divide(BigDecimal.valueOf(100), 3, RoundingMode.HALF_UP)
        );

        ReceiveIngredient receiveIngredient = new ReceiveIngredient();
        receiveIngredient.setWarehouse(warehouse);
        receiveIngredient.setIngredient(ingredient);
        receiveIngredient.setCompanyId(request.getCompanyId());
        receiveIngredient.setPrice(selfWorthPrice);
        receiveIngredient.setImportDate(request.getImportDate());
        receiveIngredient.setManufacturingDate(request.getManufacturingDate());
        receiveIngredient.setExpirationDate(request.getManufacturingDate().plusDays(ingredient.getExpiration()));
        receiveIngredient.setQuantity(totalCount);

        double totalBefore = getOrCreateIngredientBalance(warehouse, ingredient).getBalance();

        ReceiveIngredient saved = receiveIngredientRepository.save(receiveIngredient);

        IngredientBalance balance = increaseBalanceForIngredient(warehouse, ingredient, totalCount);

        IngredientDetail detail = new IngredientDetail();
        detail.setPrice(selfWorthPrice);
        detail.setImportDate(request.getImportDate());
        detail.setManufacturingDate(request.getManufacturingDate());
        detail.setExpirationDate(request.getManufacturingDate().plusDays(ingredient.getExpiration()));
        detail.setQuantity(totalCount);
        detail.setIngredientBalance(balance);
        ingredientDetailRepository.save(detail);

        if (totalCount > 0) {
            String warehouseNote = saved.getWarehouse() != null && saved.getWarehouse().getId() != null
                    ? " to warehouse id=" + saved.getWarehouse().getId()
                    : "";
            ingredientHistoryService.recordQuantityChange(
                    ingredient,
                    totalBefore,
                    totalBefore + totalCount,
                    "INCREASE",
                    "Added new inventory item" + warehouseNote,
                    selfWorthPrice,
                    request.getCompanyId()
            );
        }

        return saved;
    }

    private ReceiveIngredientResponse toReceiveIngredientResponse(ReceiveIngredient item) {
        if (item == null) return null;
        ReceiveIngredientResponse resp = new ReceiveIngredientResponse();
        resp.setId(item.getId());
        resp.setQuantity(item.getQuantity());
        Warehouse wh = item.getWarehouse();
        if (wh != null) {
            resp.setWarehouseId(wh.getId());
            resp.setWarehouseName(wh.getName());
        }
        Ingredient ing = item.getIngredient();
        if (ing != null) {
            resp.setIngredientId(ing.getId());
            resp.setIngredientName(ing.getName());
        }
        resp.setImportDate(item.getImportDate());
        resp.setExpirationDate(item.getExpirationDate());
        resp.setManufacturingDate(item.getManufacturingDate());
        Long cid = item.getCompanyId();
        resp.setCompanyId(cid);
        if (cid != null) {
            try {
                ApiResponse<String> api = companyClient.getCompanyName(cid);
                if (api != null && Boolean.TRUE.equals(api.getSuccess())) {
                    resp.setCompanyName(api.getData());
                } else if (api != null && api.getData() != null) {
                    resp.setCompanyName(api.getData());
                }
            } catch (Exception ignored) {
            }
        }
        resp.setPrice(item.getPrice());
        return resp;
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<ReceiveIngredientResponse>, PaginationMetadata> getByIngredientId(Long ingredientId, Map<String, FilterCriteria> filters,
                                                                                       Integer page,
                                                                                       Integer size,
                                                                                       String sortBy,
                                                                                       String sortDir) {
        ingredientRepository.findById(ingredientId).orElseThrow(() -> new NotFoundException("Ingredient not found"));

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<ReceiveIngredient> spec = ReceiveIngredientSpecification.buildSpecification(filters)
                .and((root, query, cb) -> root.join("ingredient", JoinType.LEFT).get("id").in(ingredientId));

        Page<ReceiveIngredient> pageResult = receiveIngredientRepository.findAll(spec, pageable);

        List<ReceiveIngredientResponse> content = pageResult.getContent()
                .stream()
                .map(item -> enrichCompany(item, receiveIngredientMapper.toSingleResponse(item)))
                .collect(Collectors.toList());

        PaginationMetadata metadata = new PaginationMetadata(
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.isLast(),
                filters,
                sortDir,
                sortBy,
                "receiveIngredientTable"
        );

        return Pair.of(content, metadata);
    }

    private ReceiveIngredientResponse enrichCompany(ReceiveIngredient item, ReceiveIngredientResponse resp) {
        if (item == null || resp == null) return resp;
        Long cid = item.getCompanyId();
        resp.setCompanyId(cid);
        if (cid != null) {
            try {
                ApiResponse<String> api = companyClient.getCompanyName(cid);
                if (api != null && Boolean.TRUE.equals(api.getSuccess())) {
                    resp.setCompanyName(api.getData());
                } else if (api != null && api.getData() != null) {
                    resp.setCompanyName(api.getData());
                }
            } catch (Exception ignored) {
            }
        }
        return resp;
    }

    private IngredientBalance getOrCreateIngredientBalance(Warehouse warehouse, Ingredient ingredient) {
        if (warehouse == null || warehouse.getId() == null) {
            throw new InvalidDataException("Warehouse is required for component balance (ingredient)");
        }
        if (ingredient == null || ingredient.getId() == null) {
            throw new InvalidDataException("Ingredient is required for component balance");
        }
        return ingredientBalanceRepository.findByWarehouseIdAndIngredientId(warehouse.getId(), ingredient.getId())
                .orElseGet(() -> {
                    IngredientBalance ib = new IngredientBalance();
                    ib.setWarehouse(warehouse);
                    ib.setIngredient(ingredient);
                    ib.setBalance(0.0);
                    return ingredientBalanceRepository.save(ib);
                });
    }

    private IngredientBalance increaseBalanceForIngredient(Warehouse warehouse, Ingredient ingredient, double qty) {
        IngredientBalance cb = getOrCreateIngredientBalance(warehouse, ingredient);
        double before = cb.getBalance() != null ? cb.getBalance() : 0.0;
        double after = before + qty;
        cb.setBalance(after);
        return ingredientBalanceRepository.save(cb);
    }
}
