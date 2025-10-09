package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.entity.*;
import com.biobac.warehouse.exception.InvalidDataException;
import com.biobac.warehouse.exception.NotFoundException;
import com.biobac.warehouse.mapper.ReceiveIngredientMapper;
import com.biobac.warehouse.repository.*;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.request.IngredientExpenseRequest;
import com.biobac.warehouse.request.ReceiveIngredientRequest;
import com.biobac.warehouse.request.ReceiveIngredientUpdateRequest;
import com.biobac.warehouse.response.ReceiveExpenseResponse;
import com.biobac.warehouse.response.ReceiveIngredientGroupResponse;
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
    private final IngredientBalanceRepository ingredientBalanceRepository;
    private final IngredientDetailRepository ingredientDetailRepository;
    private final ExpenseTypeRepository expenseTypeRepository;
    private final ReceiveExpenseRepository receiveExpenseRepository;

    @Override
    @Transactional
    public List<ReceiveIngredientResponse> receive(
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

        Long groupId = System.currentTimeMillis();
        for (ReceiveIngredientRequest req : requests) {
            ReceiveIngredient receiveIngredient =
                    createSingleReceiveItem(req, additionalExpense, receivedExpense, groupId);

            for (IngredientExpenseRequest expReq : expenseRequests) {
                ExpenseType expenseType = expenseTypeRepository.findById(expReq.getExpenseTypeId())
                        .orElseThrow(() -> new NotFoundException("Expense type not found"));

                ReceiveExpense receiveExpense = new ReceiveExpense();
                receiveExpense.setReceiveIngredient(receiveIngredient);
                receiveExpense.setExpenseType(expenseType);
                receiveExpense.setAmount(expReq.getAmount());

                receiveExpenseRepository.save(receiveExpense);
            }

            responses.add(receiveIngredientMapper.toSingleResponse(receiveIngredient));
        }

        return responses;
    }

    private ReceiveIngredient createSingleReceiveItem(
            ReceiveIngredientRequest request,
            BigDecimal additionalExpense,
            BigDecimal receivedExpense,
            Long groupId) {

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
        receiveIngredient.setGroupId(groupId);
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
        detail.setReceiveIngredient(saved);
        ingredientDetailRepository.save(detail);
        saved.setDetail(detail);

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
                .map(receiveIngredientMapper::toSingleResponse)
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

    @Override
    @Transactional(readOnly = true)
    public ReceiveIngredientGroupResponse getByGroupId(Long groupId) {
        List<ReceiveIngredient> items = receiveIngredientRepository.findByGroupId(groupId);
        if (items == null || items.isEmpty()) {
            throw new NotFoundException("Receive group not found");
        }

        List<ReceiveIngredientResponse> itemResponses = items.stream()
                .map(receiveIngredientMapper::toSingleResponse)
                .collect(Collectors.toList());

        List<ReceiveExpenseResponse> expenseResponses = new ArrayList<>();
        for (ReceiveIngredient item : items) {
            if (item.getExpenses() != null) {
                for (ReceiveExpense exp : item.getExpenses()) {
                    ReceiveExpenseResponse er = new ReceiveExpenseResponse();
                    er.setId(exp.getId());
                    er.setReceiveIngredientId(item.getId());
                    ExpenseType et = exp.getExpenseType();
                    if (et != null) {
                        er.setExpenseTypeId(et.getId());
                        er.setExpenseTypeName(et.getName());
                    }
                    er.setAmount(exp.getAmount());
                    expenseResponses.add(er);
                }
            }
        }

        ReceiveIngredientGroupResponse response = new ReceiveIngredientGroupResponse();
        response.setGroupId(groupId);
        response.setItems(itemResponses);
        response.setExpenses(expenseResponses);
        return response;
    }

    @Override
    @Transactional
    public List<ReceiveIngredientResponse> update(Long groupId, List<ReceiveIngredientUpdateRequest> request, List<IngredientExpenseRequest> expenseRequests) {
        List<ReceiveIngredient> existingGroupItems = receiveIngredientRepository.findByGroupId(groupId);
        if (existingGroupItems == null || existingGroupItems.isEmpty()) {
            throw new NotFoundException("Receive group not found");
        }
        if (request == null || request.isEmpty()) {
            throw new InvalidDataException("Update request cannot be empty");
        }

        Map<Long, ReceiveIngredient> byId = existingGroupItems.stream()
                .collect(Collectors.toMap(ReceiveIngredient::getId, it -> it));

        BigDecimal additionalExpense = expenseRequests == null ? BigDecimal.ZERO : expenseRequests.stream()
                .map(IngredientExpenseRequest::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal receivedExpense = BigDecimal.ZERO;
        for (ReceiveIngredientUpdateRequest r : request) {
            ReceiveIngredient current = byId.get(r.getId());
            if (current == null) {
                throw new InvalidDataException("Update request contains item that does not belong to the specified group");
            }
            BigDecimal price = r.getPrice() != null ? r.getPrice() : (current.getPrice() != null ? current.getPrice() : BigDecimal.ZERO);
            Double qty = r.getQuantity() != null ? r.getQuantity() : (current.getQuantity() != null ? current.getQuantity() : 0.0);
            receivedExpense = receivedExpense.add(price.multiply(BigDecimal.valueOf(qty)));
        }

        List<ReceiveIngredientResponse> responses = new ArrayList<>();

        for (ReceiveIngredientUpdateRequest r : request) {
            if (r.getId() == null || !byId.containsKey(r.getId())) {
                throw new InvalidDataException("Update request contains item that does not belong to the specified group");
            }

            ReceiveIngredient item = byId.get(r.getId());

            Ingredient newIngredient = item.getIngredient();
            if (r.getIngredientId() != null && !Objects.equals(r.getIngredientId(), newIngredient != null ? newIngredient.getId() : null)) {
                newIngredient = ingredientRepository.findById(r.getIngredientId())
                        .orElseThrow(() -> new NotFoundException("Ingredient not found"));
            }
            Warehouse newWarehouse = item.getWarehouse();
            if (r.getWarehouseId() != null && !Objects.equals(r.getWarehouseId(), newWarehouse != null ? newWarehouse.getId() : null)) {
                newWarehouse = warehouseRepository.findById(r.getWarehouseId())
                        .orElseThrow(() -> new NotFoundException("Warehouse not found"));
            }

            Double oldQty = item.getQuantity() != null ? item.getQuantity() : 0.0;
            Double newQty = r.getQuantity() != null ? r.getQuantity() : oldQty;

            BigDecimal basePrice = r.getPrice() != null ? r.getPrice() : (item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO);
            BigDecimal ingredientTotal = basePrice.multiply(BigDecimal.valueOf(newQty));
            BigDecimal proportionalExpense = BigDecimal.ZERO;
            if (receivedExpense.compareTo(BigDecimal.ZERO) > 0) {
                proportionalExpense = ingredientTotal
                        .divide(receivedExpense, 3, RoundingMode.HALF_UP)
                        .multiply(additionalExpense);
            }
            BigDecimal selfWorthPrice = basePrice.add(
                    proportionalExpense.divide(BigDecimal.valueOf(100), 3, RoundingMode.HALF_UP)
            );

            if (r.getImportDate() != null) {
                item.setImportDate(r.getImportDate());
            }
            if (r.getManufacturingDate() != null) {
                item.setManufacturingDate(r.getManufacturingDate());
            }

            Ingredient effectiveIngredient = newIngredient;
            if (item.getManufacturingDate() != null && effectiveIngredient != null && effectiveIngredient.getExpiration() != null) {
                item.setExpirationDate(item.getManufacturingDate().plusDays(effectiveIngredient.getExpiration()));
            }

            if (r.getCompanyId() != null) {
                item.setCompanyId(r.getCompanyId());
            }

            item.setPrice(selfWorthPrice);

            Warehouse oldWarehouse = item.getWarehouse();
            Ingredient oldIngredient = item.getIngredient();
            boolean locationChanged = (oldWarehouse == null || !Objects.equals(oldWarehouse.getId(), newWarehouse.getId()))
                    || (oldIngredient == null || !Objects.equals(oldIngredient.getId(), newIngredient.getId()));

            if (locationChanged) {
                if (oldWarehouse != null && oldIngredient != null) {
                    IngredientBalance oldBalance = getOrCreateIngredientBalance(oldWarehouse, oldIngredient);
                    double before = oldBalance.getBalance() != null ? oldBalance.getBalance() : 0.0;
                    oldBalance.setBalance(before - oldQty);
                    ingredientBalanceRepository.save(oldBalance);
                    ingredientHistoryService.recordQuantityChange(
                            oldIngredient,
                            before,
                            before - oldQty,
                            "DECREASE",
                            "Moved inventory from warehouse id=" + oldWarehouse.getId(),
                            item.getPrice(),
                            item.getCompanyId()
                    );
                }

                IngredientBalance newBalance = getOrCreateIngredientBalance(newWarehouse, newIngredient);
                double beforeNew = newBalance.getBalance() != null ? newBalance.getBalance() : 0.0;
                newBalance.setBalance(beforeNew + newQty);
                ingredientBalanceRepository.save(newBalance);
                ingredientHistoryService.recordQuantityChange(
                        newIngredient,
                        beforeNew,
                        beforeNew + newQty,
                        "INCREASE",
                        "Moved inventory to warehouse id=" + newWarehouse.getId(),
                        selfWorthPrice,
                        item.getCompanyId()
                );

                IngredientDetail detail = item.getDetail();
                if (detail == null) {
                    detail = new IngredientDetail();
                    detail.setReceiveIngredient(item);
                }
                detail.setIngredientBalance(newBalance);
                detail.setPrice(selfWorthPrice);
                detail.setImportDate(item.getImportDate());
                detail.setManufacturingDate(item.getManufacturingDate());
                if (item.getManufacturingDate() != null && effectiveIngredient != null && effectiveIngredient.getExpiration() != null) {
                    detail.setExpirationDate(item.getManufacturingDate().plusDays(effectiveIngredient.getExpiration()));
                }
                detail.setQuantity(newQty);
                ingredientDetailRepository.save(detail);
                item.setDetail(detail);
                item.setIngredient(newIngredient);
                item.setWarehouse(newWarehouse);
                item.setQuantity(newQty);
            } else {
                IngredientBalance balance = getOrCreateIngredientBalance(newWarehouse, newIngredient);
                double before = balance.getBalance() != null ? balance.getBalance() : 0.0;
                double delta = (newQty != null ? newQty : 0.0) - (oldQty != null ? oldQty : 0.0);
                double after = before + delta;
                balance.setBalance(after);
                ingredientBalanceRepository.save(balance);
                if (delta != 0.0) {
                    ingredientHistoryService.recordQuantityChange(
                            newIngredient,
                            before,
                            after,
                            delta > 0 ? "INCREASE" : "DECREASE",
                            (delta > 0 ? "Increased" : "Decreased") + " inventory in warehouse id=" + newWarehouse.getId(),
                            selfWorthPrice,
                            item.getCompanyId()
                    );
                }

                IngredientDetail detail = item.getDetail();
                if (detail == null) {
                    detail = new IngredientDetail();
                    detail.setReceiveIngredient(item);
                    detail.setIngredientBalance(balance);
                } else {
                    detail.setIngredientBalance(balance);
                }
                detail.setPrice(selfWorthPrice);
                detail.setImportDate(item.getImportDate());
                detail.setManufacturingDate(item.getManufacturingDate());
                if (item.getManufacturingDate() != null && effectiveIngredient != null && effectiveIngredient.getExpiration() != null) {
                    detail.setExpirationDate(item.getManufacturingDate().plusDays(effectiveIngredient.getExpiration()));
                }
                detail.setQuantity(newQty);
                ingredientDetailRepository.save(detail);
                item.setQuantity(newQty);
            }

            item.setGroupId(groupId);

            ReceiveIngredient saved = receiveIngredientRepository.save(item);

            if (saved.getExpenses() != null && !saved.getExpenses().isEmpty()) {
                receiveExpenseRepository.deleteAll(saved.getExpenses());
                saved.getExpenses().clear();
            }
            if (expenseRequests != null) {
                for (IngredientExpenseRequest expReq : expenseRequests) {
                    ExpenseType expenseType = expenseTypeRepository.findById(expReq.getExpenseTypeId())
                            .orElseThrow(() -> new NotFoundException("Expense type not found"));
                    ReceiveExpense receiveExpense = new ReceiveExpense();
                    receiveExpense.setReceiveIngredient(saved);
                    receiveExpense.setExpenseType(expenseType);
                    receiveExpense.setAmount(expReq.getAmount());
                    receiveExpenseRepository.save(receiveExpense);
                }
            }

            responses.add(receiveIngredientMapper.toSingleResponse(saved));
        }

        return responses;
    }

    @Override
    @Transactional
    public void delete(Long groupId) {
        List<ReceiveIngredient> items = receiveIngredientRepository.findByGroupId(groupId);
        if (items == null || items.isEmpty()) {
            throw new NotFoundException("Receive group not found");
        }

        for (ReceiveIngredient item : items) {
            item.setDeleted(true);

            IngredientDetail detail = item.getDetail();
            if (detail != null) {
                IngredientBalance balance = detail.getIngredientBalance();
                balance.setBalance(balance.getBalance() - detail.getQuantity());
                ingredientBalanceRepository.save(balance);
                ingredientDetailRepository.delete(detail);
                item.setDetail(null);
            }

            receiveIngredientRepository.save(item);
        }
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
