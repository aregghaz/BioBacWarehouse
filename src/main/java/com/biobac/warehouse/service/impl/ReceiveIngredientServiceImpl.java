package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.entity.*;
import com.biobac.warehouse.exception.InvalidDataException;
import com.biobac.warehouse.exception.NotFoundException;
import com.biobac.warehouse.mapper.ReceiveIngredientMapper;
import com.biobac.warehouse.repository.*;
import com.biobac.warehouse.request.*;
import com.biobac.warehouse.response.*;
import com.biobac.warehouse.service.IngredientHistoryService;
import com.biobac.warehouse.service.ReceiveIngredientService;
import com.biobac.warehouse.utils.GroupUtil;
import com.biobac.warehouse.utils.SecurityUtil;
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
    private final ReceiveGroupRepository receiveGroupRepository;
    private final ReceiveIngredientStatusRepository receiveIngredientStatusRepository;
    private final SecurityUtil securityUtil;
    private final GroupUtil groupUtil;

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final String DEFAULT_SORT_BY = "id";
    private static final String DEFAULT_SORT_DIR = "desc";

    private Pageable buildPageable(Integer page, Integer size, String sortBy, String sortDir) {
        int safePage = (page == null || page < 0) ? DEFAULT_PAGE : page;
        int safeSize = (size == null || size <= 0) ? DEFAULT_SIZE : size;
        if (safeSize > 1000) safeSize = 1000;

        String safeSortBy = (sortBy == null || sortBy.isBlank()) ? DEFAULT_SORT_BY : sortBy.trim();
        String safeSortDir = (sortDir == null || sortDir.isBlank()) ? DEFAULT_SORT_DIR : sortDir.trim();

        String mappedSortBy = mapSortField(safeSortBy);

        Sort sort = safeSortDir.equalsIgnoreCase("asc")
                ? Sort.by(mappedSortBy).ascending()
                : Sort.by(mappedSortBy).descending();

        return PageRequest.of(safePage, safeSize, sort);
    }

    private String mapSortField(String sortBy) {
        return switch (sortBy) {
            case "ingredientName" -> "ingredient.name";
            case "unitName" -> "ingredient.unit.name";
            case "status" -> "status.name";
            default -> sortBy;
        };
    }

    private static final String STATUS_COMPLETED = "завершенные";
    private static final String STATUS_NOT_DELIVERED = "не доставлено";
    private static final String STATUS_PRICE_MISMATCH = "цена не совпадает";
    private static final String STATUS_QTY_MISMATCH = "количество не совпадает";
    private static final String STATUS_BOTH_MISMATCH = "количество и цена не совпадает";

    private ReceiveIngredientStatus getStatusByName(String name) {
        return receiveIngredientStatusRepository.findByName(name)
                .orElseThrow(() -> new NotFoundException("Receive ingredient status not found: " + name));
    }

    private boolean pricesEqual(BigDecimal a, BigDecimal b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.compareTo(b) == 0;
    }

    private boolean qtyEqual(Double a, Double b) {
        double aa = a == null ? 0.0 : a;
        double bb = b == null ? 0.0 : b;
        return Math.abs(aa - bb) < 1e-9;
    }

    private void updateStatusFor(ReceiveIngredient item, BigDecimal confirmedPrice) {
        Double plannedQty = item.getQuantity();
        Double receivedQty = item.getReceivedQuantity();
        boolean qtyMatch = qtyEqual(plannedQty, receivedQty);
        boolean priceMatch = confirmedPrice == null || pricesEqual(item.getPrice(), confirmedPrice);

        if (qtyMatch && priceMatch) {
            item.setStatus(getStatusByName(STATUS_COMPLETED));
        } else if (!qtyMatch && !priceMatch) {
            item.setStatus(getStatusByName(STATUS_BOTH_MISMATCH));
        } else if (!qtyMatch) {
            item.setStatus(getStatusByName(STATUS_QTY_MISMATCH));
        } else if (!priceMatch) {
            item.setStatus(getStatusByName(STATUS_PRICE_MISMATCH));
        } else {
            item.setStatus(getStatusByName(STATUS_NOT_DELIVERED));
        }
    }

    private boolean isCompleted(ReceiveIngredient item) {
        ReceiveIngredientStatus st = item.getStatus();
        return st != null && STATUS_COMPLETED.equals(st.getName());
    }

    @Override
    @Transactional
    public List<ReceiveIngredientResponse> receive(
            List<ReceiveIngredientRequest> requests,
            List<IngredientExpenseRequest> expenseRequests) {

        BigDecimal additionalExpense = (expenseRequests == null ? new ArrayList<IngredientExpenseRequest>() : expenseRequests).stream()
                .map(IngredientExpenseRequest::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal receivedExpense = requests.stream()
                .map(r -> r.getPrice().multiply(BigDecimal.valueOf(r.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<ReceiveIngredientResponse> responses = new ArrayList<>();

        ReceiveGroup group = new ReceiveGroup();
        group = receiveGroupRepository.save(group);
        Long groupId = group.getId();
        for (ReceiveIngredientRequest req : requests) {
            ReceiveIngredient receiveIngredient =
                    createSingleReceiveItem(req, additionalExpense, receivedExpense, group);
            Ingredient currentIngredient = receiveIngredient.getIngredient();
            currentIngredient.setPrice(req.getPrice());
            ingredientRepository.save(currentIngredient);

            responses.add(receiveIngredientMapper.toSingleResponse(receiveIngredient));
        }

        if (expenseRequests != null) {
            for (IngredientExpenseRequest expReq : expenseRequests) {
                ExpenseType expenseType = expenseTypeRepository.findById(expReq.getExpenseTypeId())
                        .orElseThrow(() -> new NotFoundException("Expense type not found"));

                ReceiveExpense receiveExpense = new ReceiveExpense();
                receiveExpense.setGroup(group);
                receiveExpense.setExpenseType(expenseType);
                receiveExpense.setAmount(expReq.getAmount());

                receiveExpenseRepository.save(receiveExpense);
            }
        }

        return responses;
    }

    @Override
    @Transactional
    public List<ReceiveIngredientResponse> finalizeReceive(Long groupId, List<ReceiveIngredientFinalizeRequest> request) {
        List<ReceiveIngredient> existingGroupItems = receiveIngredientRepository.findByGroupId(groupId);
        if (existingGroupItems == null || existingGroupItems.isEmpty()) {
            throw new NotFoundException("Receive group not found");
        }
        if (request == null || request.isEmpty()) {
            throw new InvalidDataException("Update request cannot be empty");
        }

        Map<Long, ReceiveIngredient> byId = existingGroupItems.stream()
                .collect(Collectors.toMap(ReceiveIngredient::getId, it -> it));

        List<ReceiveIngredientResponse> responses = new ArrayList<>();

        List<ReceiveExpense> groupExpenses = receiveExpenseRepository.findByGroupId(groupId);
        BigDecimal additionalExpense = (groupExpenses == null ? new java.util.ArrayList<ReceiveExpense>() : groupExpenses).stream()
                .map(ReceiveExpense::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalBase = existingGroupItems.stream()
                .map(it -> {
                    BigDecimal p = it.getPrice() != null ? it.getPrice() : BigDecimal.ZERO;
                    double q = it.getQuantity() != null ? it.getQuantity() : 0.0;
                    return p.multiply(BigDecimal.valueOf(q));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        for (ReceiveIngredientFinalizeRequest r : request) {
            ReceiveIngredient current = byId.get(r.getId());
            if (current == null) {
                throw new InvalidDataException("Finalize request contains item that does not belong to the specified group");
            }

            Ingredient ingredient = current.getIngredient();
            Warehouse warehouse = current.getWarehouse();

            current.setImportDate(r.getImportDate());
            current.setManufacturingDate(r.getManufacturingDate());
            current.setLastPrice(r.getConfirmedPrice());
            if (current.getManufacturingDate() != null && ingredient != null && ingredient.getExpiration() != null) {
                current.setExpirationDate(current.getManufacturingDate().plusDays(ingredient.getExpiration()));
            }

            double alreadyReceived = current.getReceivedQuantity() == null ? 0.0 : current.getReceivedQuantity();
            double delta = r.getReceivedQuantity() == null ? 0.0 : r.getReceivedQuantity();

            IngredientBalance balance = getOrCreateIngredientBalance(warehouse, ingredient);
            double before = balance.getBalance() != null ? balance.getBalance() : 0.0;
            double after = before + delta;
            balance.setBalance(after);
            ingredientBalanceRepository.save(balance);

            BigDecimal basePrice = current.getPrice() != null ? current.getPrice() : BigDecimal.ZERO;
            BigDecimal plannedQtyBD = BigDecimal.valueOf(current.getQuantity() != null ? current.getQuantity() : 0.0);
            BigDecimal itemBaseAmount = basePrice.multiply(plannedQtyBD);
            BigDecimal additionalPerUnit = BigDecimal.ZERO;
            if (additionalExpense.compareTo(BigDecimal.ZERO) > 0
                    && totalBase.compareTo(BigDecimal.ZERO) > 0
                    && plannedQtyBD.compareTo(BigDecimal.ZERO) > 0) {
                additionalPerUnit = itemBaseAmount
                        .divide(totalBase, 8, RoundingMode.HALF_EVEN)
                        .multiply(additionalExpense)
                        .divide(plannedQtyBD, 2, RoundingMode.HALF_EVEN);
            }
            BigDecimal detailPrice = basePrice.add(additionalPerUnit);
            IngredientDetail detail = current.getDetail();
            if (detail == null) {
                detail = new IngredientDetail();
                detail.setReceiveIngredient(current);
                detail.setQuantity(0.0);
            }
            detail.setIngredientBalance(balance);
            detail.setPrice(detailPrice);
            detail.setImportDate(current.getImportDate());
            detail.setManufacturingDate(current.getManufacturingDate());
            if (current.getManufacturingDate() != null && ingredient != null && ingredient.getExpiration() != null) {
                detail.setExpirationDate(current.getManufacturingDate().plusDays(ingredient.getExpiration()));
            }
            double detailQty = detail.getQuantity() == null ? 0.0 : detail.getQuantity();
            detail.setQuantity(detailQty + delta);
            ingredientDetailRepository.save(detail);
            current.setDetail(detail);

            if (r.getReceivedQuantity() > 0) {
                ingredientHistoryService.recordQuantityChange(
                        current.getImportDate(),
                        ingredient,
                        before,
                        after,
                        String.format("Получено +%s на склад %s", delta, warehouse.getName()),
                        ingredient.getPrice(),
                        current.getCompanyId()
                );
            }

            current.setReceivedQuantity(alreadyReceived + delta);
            updateStatusFor(current, r.getConfirmedPrice());

            ReceiveIngredient saved = receiveIngredientRepository.save(current);
            responses.add(receiveIngredientMapper.toSingleResponse(saved));
        }

        return responses;
    }

    private ReceiveIngredient createSingleReceiveItem(
            ReceiveIngredientRequest request,
            BigDecimal additionalExpense,
            BigDecimal receivedExpense,
            ReceiveGroup group) {

        Ingredient ingredient = ingredientRepository.findById(request.getIngredientId())
                .orElseThrow(() -> new NotFoundException("Ingredient not found"));

        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new NotFoundException("Warehouse not found"));

        double totalCount = request.getQuantity();

        BigDecimal basePrice = request.getPrice();

        ReceiveIngredient receiveIngredient = new ReceiveIngredient();
        receiveIngredient.setWarehouse(warehouse);
        receiveIngredient.setIngredient(ingredient);
        receiveIngredient.setCompanyId(request.getCompanyId());
        receiveIngredient.setGroup(group);
        receiveIngredient.setPrice(basePrice);
        receiveIngredient.setImportDate(null);
        receiveIngredient.setManufacturingDate(null);
        receiveIngredient.setExpirationDate(null);
        receiveIngredient.setQuantity(totalCount);
        receiveIngredient.setReceivedQuantity(0.0);
        receiveIngredient.setStatus(getStatusByName(STATUS_NOT_DELIVERED));

        return receiveIngredientRepository.save(receiveIngredient);
    }


    @Override
    @Transactional(readOnly = true)
    public Pair<List<ReceiveIngredientResponse>, PaginationMetadata> getPagination(Map<String, FilterCriteria> filters,
                                                                                   Integer page,
                                                                                   Integer size,
                                                                                   String sortBy,
                                                                                   String sortDir) {
        List<Long> warehouseGroupIds = groupUtil.getAccessibleWarehouseGroupIds();
        List<Long> ingredientGroupIds = groupUtil.getAccessibleIngredientGroupIds();

        Pageable pageable = buildPageable(page, size, sortBy, sortDir);

        Specification<ReceiveIngredient> spec = ReceiveIngredientSpecification.buildSpecification(filters)
                .and(ReceiveIngredientSpecification.belongsToWarehouseGroups(warehouseGroupIds))
                .and(ReceiveIngredientSpecification.belongsToIngredientGroups(ingredientGroupIds));

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
    public Pair<List<ReceiveIngredientResponse>, PaginationMetadata> getSucceeded(Map<String, FilterCriteria> filters,
                                                                                  Integer page,
                                                                                  Integer size,
                                                                                  String sortBy,
                                                                                  String sortDir) {
        return getBySucceed(true, filters, page, size, sortBy, sortDir);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<ReceiveIngredientResponse>, PaginationMetadata> getPending(Map<String, FilterCriteria> filters,
                                                                                Integer page,
                                                                                Integer size,
                                                                                String sortBy,
                                                                                String sortDir) {
        return getBySucceed(false, filters, page, size, sortBy, sortDir);
    }

    private Pair<List<ReceiveIngredientResponse>, PaginationMetadata> getBySucceed(boolean succeed,
                                                                                   Map<String, FilterCriteria> filters,
                                                                                   Integer page,
                                                                                   Integer size,
                                                                                   String sortBy,
                                                                                   String sortDir) {
        List<Long> warehouseGroupIds = groupUtil.getAccessibleWarehouseGroupIds();
        List<Long> ingredientGroupIds = groupUtil.getAccessibleIngredientGroupIds();
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);

        Specification<ReceiveIngredient> spec = ReceiveIngredientSpecification.buildSpecification(filters)
                .and((root, query, cb) -> {
                    var statusJoin = root.join("status", JoinType.LEFT);
                    if (succeed) {
                        return cb.equal(statusJoin.get("name"), STATUS_COMPLETED);
                    } else {
                        return cb.notEqual(statusJoin.get("name"), STATUS_COMPLETED);
                    }
                })
                .and(ReceiveIngredientSpecification.belongsToWarehouseGroups(warehouseGroupIds))
                .and(ReceiveIngredientSpecification.belongsToIngredientGroups(ingredientGroupIds));

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

        List<ReceiveExpense> groupExpenses = receiveExpenseRepository.findByGroupId(groupId);
        List<ReceiveExpenseResponse> expenseResponses = new ArrayList<>();
        for (ReceiveExpense exp : groupExpenses) {
            ReceiveExpenseResponse er = new ReceiveExpenseResponse();
            er.setId(exp.getId());
            ExpenseType et = exp.getExpenseType();
            if (et != null) {
                er.setExpenseTypeId(et.getId());
                er.setExpenseTypeName(et.getName());
            }
            er.setAmount(exp.getAmount());
            expenseResponses.add(er);
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

        ReceiveGroup group = receiveGroupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Receive group not found"));

        boolean canReceiveUpdate =
                securityUtil.hasPermission("RECEIVE_INGREDIENT_STATUS_UPDATE");

        Map<Long, ReceiveIngredient> byId = existingGroupItems.stream()
                .collect(Collectors.toMap(ReceiveIngredient::getId, it -> it));

        BigDecimal additionalExpense = expenseRequests == null ? BigDecimal.ZERO : expenseRequests.stream()
                .map(IngredientExpenseRequest::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal receivedExpense = BigDecimal.ZERO;
        for (ReceiveIngredientUpdateRequest r : request) {
            if (r.getId() != null) {
                ReceiveIngredient current = byId.get(r.getId());
                if (isCompleted(current) && canReceiveUpdate) {
                    throw new InvalidDataException("Only pending receive ingredients can be updated");
                }
                BigDecimal price = r.getPrice() != null ? r.getPrice() : (current.getPrice() != null ? current.getPrice() : BigDecimal.ZERO);
                double qty = r.getQuantity() != null ? r.getQuantity() : (current.getQuantity() != null ? current.getQuantity() : 0.0);
                receivedExpense = receivedExpense.add(price.multiply(BigDecimal.valueOf(qty)));
            } else {
                if (r.getPrice() == null || r.getQuantity() == null) {
                    throw new InvalidDataException("New items must provide price and quantity");
                }
                receivedExpense = receivedExpense.add(r.getPrice().multiply(BigDecimal.valueOf(r.getQuantity())));
            }
        }

        List<ReceiveIngredientResponse> responses = new ArrayList<>();
        java.util.Set<Long> processedExistingIds = new java.util.HashSet<>();

        for (ReceiveIngredientUpdateRequest r : request) {
            if (r.getId() == null) {
                if (r.getIngredientId() == null) throw new InvalidDataException("Ingredient is required for new item");
                if (r.getWarehouseId() == null) throw new InvalidDataException("Warehouse is required for new item");
                if (r.getQuantity() == null) throw new InvalidDataException("Quantity is required for new item");
                if (r.getPrice() == null) throw new InvalidDataException("Price is required for new item");

                Ingredient ingredient = ingredientRepository.findById(r.getIngredientId())
                        .orElseThrow(() -> new NotFoundException("Ingredient not found"));
                Warehouse warehouse = warehouseRepository.findById(r.getWarehouseId())
                        .orElseThrow(() -> new NotFoundException("Warehouse not found"));

                double newQty = r.getQuantity();
                BigDecimal basePrice = r.getPrice();

                ReceiveIngredient item = new ReceiveIngredient();
                item.setIngredient(ingredient);
                item.setWarehouse(warehouse);
                item.setCompanyId(r.getCompanyId());
                item.setImportDate(r.getImportDate());
                item.setManufacturingDate(r.getManufacturingDate());
                if (item.getManufacturingDate() != null && ingredient.getExpiration() != null) {
                    item.setExpirationDate(item.getManufacturingDate().plusDays(ingredient.getExpiration()));
                }
                item.setQuantity(newQty);
                item.setPrice(basePrice);
                item.setGroup(group);
                item.setReceivedQuantity(0.0);
                if (canReceiveUpdate && r.getStatusId() != null) {
                    ReceiveIngredientStatus st = receiveIngredientStatusRepository.findById(r.getStatusId())
                            .orElseThrow(() -> new NotFoundException("Status not found"));
                    item.setStatus(st);
                } else {
                    item.setStatus(getStatusByName(STATUS_NOT_DELIVERED));
                }

                ReceiveIngredient saved = receiveIngredientRepository.save(item);
                responses.add(receiveIngredientMapper.toSingleResponse(saved));
            } else {
                ReceiveIngredient item = byId.get(r.getId());
                if (canReceiveUpdate && r.getStatusId() != null) {
                    throw new InvalidDataException("Only admin can update status");
                }

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

                double oldQty = item.getQuantity() != null ? item.getQuantity() : 0.0;
                double newQty = r.getQuantity() != null ? r.getQuantity() : oldQty;

                BigDecimal basePrice = r.getPrice() != null ? r.getPrice() : (item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO);

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

                if (canReceiveUpdate && r.getStatusId() != null) {
                    ReceiveIngredientStatus st = receiveIngredientStatusRepository.findById(r.getStatusId())
                            .orElseThrow(() -> new NotFoundException("Status not found"));
                    item.setStatus(st);
                }

                item.setPrice(basePrice);
                item.setIngredient(newIngredient);
                item.setWarehouse(newWarehouse);
                item.setQuantity(newQty);
                item.setGroup(group);

                ReceiveIngredient saved = receiveIngredientRepository.save(item);
                responses.add(receiveIngredientMapper.toSingleResponse(saved));
                processedExistingIds.add(item.getId());
            }
        }

        for (ReceiveIngredient oldItem : existingGroupItems) {
            if (!oldItem.isDeleted() && !processedExistingIds.contains(oldItem.getId())) {
                if (canReceiveUpdate || !isCompleted(oldItem)) {
                    oldItem.setDeleted(true);
                    receiveIngredientRepository.save(oldItem);
                }
            }
        }

        if (expenseRequests != null) {
            receiveExpenseRepository.deleteByGroupId(groupId);
            for (IngredientExpenseRequest expReq : expenseRequests) {
                ExpenseType expenseType = expenseTypeRepository.findById(expReq.getExpenseTypeId())
                        .orElseThrow(() -> new NotFoundException("Expense type not found"));
                ReceiveExpense receiveExpense = new ReceiveExpense();
                receiveExpense.setGroup(group);
                receiveExpense.setExpenseType(expenseType);
                receiveExpense.setAmount(expReq.getAmount());
                receiveExpenseRepository.save(receiveExpense);
            }
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

        boolean hasSucceeded = items.stream().anyMatch(this::isCompleted);
        if (hasSucceeded) {
            throw new InvalidDataException("Only pending receive ingredients can be deleted");
        }

        receiveExpenseRepository.deleteByGroupId(groupId);

        for (ReceiveIngredient item : items) {
            item.setDeleted(true);
            receiveIngredientRepository.save(item);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ReceiveIngredientsPriceCalcResponse calcIngredientPrices(List<ReceiveIngredientsPriceCalcRequest> ingredients, List<IngredientExpenseRequest> expenses) {
        if (ingredients == null || ingredients.isEmpty()) {
            throw new InvalidDataException("Ingredients list cannot be empty");
        }

        BigDecimal totalBase = BigDecimal.ZERO;
        BigDecimal totalWithoutExpense = BigDecimal.ZERO;
        for (ReceiveIngredientsPriceCalcRequest r : ingredients) {
            if (r.getIngredientId() == null) throw new InvalidDataException("Ingredient id is required");
            BigDecimal price = r.getPrice() != null ? r.getPrice() : BigDecimal.ZERO;
            double qty = r.getQuantity() != null ? r.getQuantity() : 0.0;
            totalBase = totalBase.add(price.multiply(BigDecimal.valueOf(qty)));
        }
        BigDecimal totalExpenses = (expenses == null ? new ArrayList<IngredientExpenseRequest>() : expenses)
                .stream()
                .map(IngredientExpenseRequest::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        ReceiveIngredientsPriceCalcResponse response = new ReceiveIngredientsPriceCalcResponse();
        List<ReceiveIngredientsPriceCalcResponse.Ingredients> ingredientResponses = new ArrayList<>();

        BigDecimal grandTotal = BigDecimal.ZERO;
        for (ReceiveIngredientsPriceCalcRequest r : ingredients) {
            Ingredient ingredient = ingredientRepository.findById(r.getIngredientId())
                    .orElseThrow(() -> new NotFoundException("Ingredient not found"));

            BigDecimal price = r.getPrice() != null ? r.getPrice() : BigDecimal.ZERO;
            double qty = r.getQuantity() != null ? r.getQuantity() : 0.0;

            BigDecimal itemBaseAmount = price.multiply(BigDecimal.valueOf(qty));
            BigDecimal additionalPerUnit = BigDecimal.ZERO;
            if (totalExpenses.compareTo(BigDecimal.ZERO) > 0
                    && totalBase.compareTo(BigDecimal.ZERO) > 0
                    && qty > 0.0) {
                additionalPerUnit = itemBaseAmount
                        .divide(totalBase, 8, RoundingMode.HALF_EVEN)
                        .multiply(totalExpenses)
                        .divide(BigDecimal.valueOf(qty), 2, RoundingMode.HALF_EVEN);
            }
            BigDecimal calculatedPrice = price.add(additionalPerUnit);
            BigDecimal total = calculatedPrice.multiply(BigDecimal.valueOf(qty)).setScale(2, RoundingMode.HALF_EVEN);
            grandTotal = grandTotal.add(total);

            ReceiveIngredientsPriceCalcResponse.Ingredients ir = new ReceiveIngredientsPriceCalcResponse.Ingredients();
            ir.setIngredientId(ingredient.getId());
            ir.setIngredientName(ingredient.getName());
            ir.setUnitName(ingredient.getUnit().getName());
            ir.setQuantity(qty);
            ir.setPrice(price.setScale(2, RoundingMode.HALF_EVEN));
            ir.setCalculatedPrice(calculatedPrice.setScale(2, RoundingMode.HALF_EVEN));
            ir.setTotal(total);
            ingredientResponses.add(ir);
        }

        response.setIngredients(ingredientResponses);
        response.setPriceWithoutExpense(totalBase);
        response.setTotalPrice(grandTotal.setScale(2, RoundingMode.HALF_EVEN));

        List<ReceiveIngredientsPriceCalcResponse.Expenses> expenseResponses = new ArrayList<>();
        if (expenses != null) {
            for (IngredientExpenseRequest er : expenses) {
                if (er.getExpenseTypeId() == null) continue;
                ExpenseType et = expenseTypeRepository.findById(er.getExpenseTypeId())
                        .orElseThrow(() -> new NotFoundException("Expense type not found"));
                ReceiveIngredientsPriceCalcResponse.Expenses ex = new ReceiveIngredientsPriceCalcResponse.Expenses();
                ex.setExpenseTypeName(et.getName());
                ex.setAmount(er.getAmount() == null ? BigDecimal.ZERO : er.getAmount().setScale(2, RoundingMode.HALF_EVEN));
                expenseResponses.add(ex);
            }
        }
        response.setExpenses(expenseResponses);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SelectResponse> getStatusesSelect() {
        return receiveIngredientStatusRepository.findAll().stream().map(s -> {
            SelectResponse response = new SelectResponse();
            response.setId(s.getId());
            response.setName(s.getName());
            return response;
        }).toList();
    }

    private IngredientBalance getOrCreateIngredientBalance(Warehouse warehouse, Ingredient ingredient) {
        if (warehouse == null || warehouse.getId() == null) {
            throw new InvalidDataException("Warehouse is required for component balance (ingredient)");
        }
        if (ingredient == null || ingredient.getId() == null) {
            throw new InvalidDataException("Ingredient is required for component balance");
        }
        return ingredientBalanceRepository.findByWarehouseAndIngredient(warehouse, ingredient)
                .orElseGet(() -> {
                    IngredientBalance ib = new IngredientBalance();
                    ib.setWarehouse(warehouse);
                    ib.setIngredient(ingredient);
                    ib.setBalance(0.0);
                    return ingredientBalanceRepository.save(ib);
                });
    }
}
