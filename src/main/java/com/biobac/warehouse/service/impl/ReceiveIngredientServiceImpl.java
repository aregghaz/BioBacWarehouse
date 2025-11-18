package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.dto.IngredientHistoryDto;
import com.biobac.warehouse.dto.IngredientPriceRecord;
import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.entity.ExpenseType;
import com.biobac.warehouse.entity.HistoryAction;
import com.biobac.warehouse.entity.Ingredient;
import com.biobac.warehouse.entity.IngredientHistory;
import com.biobac.warehouse.entity.ReceiveExpense;
import com.biobac.warehouse.entity.ReceiveGroup;
import com.biobac.warehouse.entity.ReceiveIngredient;
import com.biobac.warehouse.entity.ReceiveIngredientStatus;
import com.biobac.warehouse.entity.Warehouse;
import com.biobac.warehouse.exception.InvalidDataException;
import com.biobac.warehouse.exception.NotFoundException;
import com.biobac.warehouse.mapper.IngredientHistoryMapper;
import com.biobac.warehouse.mapper.ReceiveIngredientMapper;
import com.biobac.warehouse.repository.HistoryActionRepository;
import com.biobac.warehouse.repository.IngredientRepository;
import com.biobac.warehouse.repository.ReceiveExpenseRepository;
import com.biobac.warehouse.repository.ReceiveGroupRepository;
import com.biobac.warehouse.repository.ReceiveIngredientRepository;
import com.biobac.warehouse.repository.ReceiveIngredientStatusRepository;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.request.IngredientExpenseRequest;
import com.biobac.warehouse.request.ReceiveIngredientFinalizeRequest;
import com.biobac.warehouse.request.ReceiveIngredientRequest;
import com.biobac.warehouse.request.ReceiveIngredientUpdateRequest;
import com.biobac.warehouse.request.ReceiveIngredientsPriceCalcRequest;
import com.biobac.warehouse.response.ReceiveExpenseResponse;
import com.biobac.warehouse.response.ReceiveIngredientGroupResponse;
import com.biobac.warehouse.response.ReceiveIngredientResponse;
import com.biobac.warehouse.response.ReceiveIngredientsPriceCalcResponse;
import com.biobac.warehouse.response.SelectResponse;
import com.biobac.warehouse.service.ExpenseTypeService;
import com.biobac.warehouse.service.IngredientHistoryService;
import com.biobac.warehouse.service.IngredientService;
import com.biobac.warehouse.service.ReceiveIngredientService;
import com.biobac.warehouse.service.WarehouseService;
import com.biobac.warehouse.utils.GroupUtil;
import com.biobac.warehouse.utils.IngredientPriceUtil;
import com.biobac.warehouse.utils.PageUtil;
import com.biobac.warehouse.utils.ReceiveIngredientCalculationUtil;
import com.biobac.warehouse.utils.ReceiveIngredientFinalizationUtil;
import com.biobac.warehouse.utils.SecurityUtil;
import com.biobac.warehouse.utils.specifications.ReceiveIngredientSpecification;
import jakarta.persistence.criteria.JoinType;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReceiveIngredientServiceImpl implements ReceiveIngredientService {
    private final IngredientRepository ingredientRepository;
    private final ReceiveIngredientRepository receiveIngredientRepository;
    private final ReceiveIngredientMapper receiveIngredientMapper;
    private final IngredientHistoryService ingredientHistoryService;
    private final ReceiveExpenseRepository receiveExpenseRepository;
    private final ReceiveGroupRepository receiveGroupRepository;
    private final ReceiveIngredientStatusRepository receiveIngredientStatusRepository;
    private final SecurityUtil securityUtil;
    private final GroupUtil groupUtil;
    private final HistoryActionRepository historyActionRepository;
    private final ExpenseTypeService expenseTypeService;
    private final ReceiveIngredientFinalizationUtil receiveIngredientFinalizationUtil;
    private final IngredientService ingredientService;
    private final WarehouseService warehouseService;
    private final IngredientHistoryMapper ingredientHistoryMapper;

    private static final String STATUS_COMPLETED = "завершенные";
    private static final String STATUS_NOT_DELIVERED = "не доставлено";
    private static final String STATUS_PRICE_MISMATCH = "цена не совпадает";
    private static final String STATUS_QTY_MISMATCH = "количество не совпадает";
    private static final String STATUS_BOTH_MISMATCH = "количество и цена не совпадает";


    @Override
    @Transactional
    public List<ReceiveIngredientResponse> receive(
            List<ReceiveIngredientRequest> requests,
            List<IngredientExpenseRequest> expenseRequests
    ) {

        // TODO unused calculations
//        BigDecimal additionalExpense = (expenseRequests == null ? new ArrayList<IngredientExpenseRequest>() : expenseRequests).stream()
//                .map(IngredientExpenseRequest::getAmount)
//                .filter(Objects::nonNull)
//                .reduce(BigDecimal.ZERO, BigDecimal::add);

//        BigDecimal receivedExpense = requests.stream()
//                .map(r -> r.getPrice().multiply(BigDecimal.valueOf(r.getQuantity())))
//                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<ReceiveIngredientResponse> responses = new ArrayList<>();

        ReceiveGroup group = new ReceiveGroup();
        group = receiveGroupRepository.save(group);

        // TODO unused group id
//    Long groupId = group.getId();
        for (ReceiveIngredientRequest req : requests) {
            ReceiveIngredient receiveIngredient = createSingleReceiveItem(req, group);
//                    createSingleReceiveItem(req, additionalExpense, receivedExpense, group);

            Ingredient currentIngredient = receiveIngredient.getIngredient();
            currentIngredient.setPrice(req.getPrice());
            ingredientRepository.save(currentIngredient);
            responses.add(receiveIngredientMapper.toSingleResponse(receiveIngredient));
        }
        if (expenseRequests != null) extractIngredientExpenseRequest(expenseRequests, group);

        return responses;
    }


    @Override
    @Transactional
    public List<ReceiveIngredientResponse> finalizeReceive(Long groupId, List<ReceiveIngredientFinalizeRequest> request) {
        List<ReceiveIngredient> existingGroupItems = extractGroupById(groupId, request);
        Map<Long, ReceiveIngredient> byId = existingGroupItems.stream()
                .collect(Collectors.toMap(ReceiveIngredient::getId, receiveIngredient -> receiveIngredient));

        List<ReceiveIngredientResponse> responses = new ArrayList<>();
        List<ReceiveExpense> groupExpenses = receiveExpenseRepository.findByGroupId(groupId);

        BigDecimal additionalExpense = ReceiveIngredientCalculationUtil.calculateTotalExpenses(groupExpenses);
        BigDecimal totalBase = ReceiveIngredientCalculationUtil.calculateTotalBaseCost(existingGroupItems);

        for (ReceiveIngredientFinalizeRequest finalizeRequest : request) {
            ReceiveIngredient currentIngredient = byId.get(finalizeRequest.getId());
            Ingredient ingredient = currentIngredient.getIngredient();
            Warehouse warehouse = currentIngredient.getWarehouse();

            currentIngredient.setImportDate(finalizeRequest.getImportDate());
            currentIngredient.setManufacturingDate(finalizeRequest.getManufacturingDate());
            currentIngredient.setLastPrice(finalizeRequest.getConfirmedPrice());

            if (currentIngredient.getManufacturingDate() != null && ingredient != null && ingredient.getExpiration() != null) {
                currentIngredient.setExpirationDate(currentIngredient.getManufacturingDate().plusDays(ingredient.getExpiration()));
            }

            receiveIngredientFinalizationUtil.finalizeReceiveIngredient(
                    currentIngredient,
                    finalizeRequest.getReceivedQuantity(),
                    warehouse,
                    ingredient,
                    additionalExpense,
                    totalBase
            );

            final double alreadyReceived = currentIngredient.getReceivedQuantity() != null ? currentIngredient.getReceivedQuantity() : 0.0;
            final double delta = finalizeRequest.getReceivedQuantity() != null ? finalizeRequest.getReceivedQuantity() : 0.0;
            currentIngredient.setReceivedQuantity(alreadyReceived + delta);

            HistoryAction action = historyActionRepository.findById(3L)
                    .orElseThrow(() -> new NotFoundException("Action not found"));

            if (finalizeRequest.getReceivedQuantity() > 0) {
//                IngredientHistoryDto ingredientHistoryDto = new IngredientHistoryDto();
//                ingredientHistoryDto.setIngredient(ingredient);
//                ingredientHistoryDto.setWarehouse(warehouse);
//                ingredientHistoryDto.setQuantityChange(delta);
//                ingredientHistoryDto.setNotes(String.format("Получено +%s на склад %s", delta, warehouse.getName()));
//                ingredientHistoryDto.setLastPrice(finalizeRequest.getConfirmedPrice());
//                ingredientHistoryDto.setAction(action);
//                ingredientHistoryDto.setLastCompanyId(currentIngredient.getCompanyId());
//                ingredientHistoryDto.setTimestamp(finalizeRequest.getImportDate());

                IngredientHistoryDto ingredientHistoryDto = IngredientHistoryDto.builder()
                        .ingredient(ingredient)
                        .warehouse(warehouse)
                        .quantityChange(delta)
                        .notes(String.format("Получено +%s на склад %s", delta, warehouse.getName()))
                        .lastPrice(finalizeRequest.getConfirmedPrice())
                        .action(action)
                        .lastCompanyId(currentIngredient.getCompanyId())
                        .timestamp(finalizeRequest.getImportDate())
                        .build();

                ingredientHistoryService.recordQuantityChange(ingredientHistoryDto);
            }

            currentIngredient.setReceivedQuantity(alreadyReceived + delta);
            updateStatusFor(currentIngredient, finalizeRequest.getConfirmedPrice());

            ReceiveIngredient saved = receiveIngredientRepository.save(currentIngredient);
            responses.add(receiveIngredientMapper.toSingleResponse(saved));
        }

        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<ReceiveIngredientResponse>, PaginationMetadata> getPagination(
            Map<String, FilterCriteria> filters,
            Integer page,
            Integer size,
            String sortBy,
            String sortDir
    ) {
        List<Long> warehouseGroupIds = groupUtil.getAccessibleWarehouseGroupIds();
        List<Long> ingredientGroupIds = groupUtil.getAccessibleIngredientGroupIds();

        Pageable pageable = PageUtil.buildPageable(page, size, sortBy, sortDir);

        Specification<ReceiveIngredient> spec = ReceiveIngredientSpecification.buildSpecification(filters)
                .and(ReceiveIngredientSpecification.belongsToWarehouseGroups(warehouseGroupIds))
                .and(ReceiveIngredientSpecification.belongsToIngredientGroups(ingredientGroupIds));

        return getListPaginationMetadataPair(filters, sortBy, sortDir, pageable, spec);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<ReceiveIngredientResponse>, PaginationMetadata> getSucceeded(
            Map<String, FilterCriteria> filters,
            Integer page,
            Integer size,
            String sortBy,
            String sortDir
    ) {
        return getBySucceed(true, filters, page, size, sortBy, sortDir);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<ReceiveIngredientResponse>, PaginationMetadata> getPending(
            Map<String, FilterCriteria> filters,
            Integer page,
            Integer size,
            String sortBy,
            String sortDir
    ) {
        return getBySucceed(false, filters, page, size, sortBy, sortDir);
    }

    private void extractIngredientExpenseRequest(List<IngredientExpenseRequest> expenseRequests, ReceiveGroup group) {
        for (IngredientExpenseRequest ingredientExpenseRequest : expenseRequests) {
            ExpenseType expenseType = expenseTypeService.getExpenseTypeById(ingredientExpenseRequest.getExpenseTypeId());
            ReceiveExpense receiveExpense = new ReceiveExpense();
            receiveExpense.setGroup(group);
            receiveExpense.setExpenseType(expenseType);
            receiveExpense.setAmount(ingredientExpenseRequest.getAmount());
            receiveExpenseRepository.save(receiveExpense);
        }
    }

    // TODO  BigDecimal receivedExpense, BigDecimal additionalExpense, unused passed parameter
    private ReceiveIngredient createSingleReceiveItem(ReceiveIngredientRequest request, ReceiveGroup group) {
        Ingredient ingredient = ingredientService.getIngredientById(request.getIngredientId());
        Warehouse warehouse = warehouseService.getWarehouseById(request.getWarehouseId());

        double totalCount = request.getQuantity();
        BigDecimal basePrice = request.getPrice();

//        ReceiveIngredient receiveIngredient = new ReceiveIngredient();
//        receiveIngredient.setWarehouse(warehouse);
//        receiveIngredient.setIngredient(ingredient);
//        receiveIngredient.setCompanyId(request.getCompanyId());
//        receiveIngredient.setGroup(group);
//        receiveIngredient.setPrice(basePrice);
//        receiveIngredient.setImportDate(null);
//        receiveIngredient.setManufacturingDate(null);
//        receiveIngredient.setExpirationDate(null);
//        receiveIngredient.setQuantity(totalCount);
//        receiveIngredient.setReceivedQuantity(0.0);
//        receiveIngredient.setStatus(getStatusByName(STATUS_NOT_DELIVERED));

        ReceiveIngredient receiveIngredient = receiveIngredientMapper.toReceiveIngredient(request, group, ingredient, warehouse);
        receiveIngredient.setQuantity(totalCount);
        receiveIngredient.setPrice(basePrice);

        return receiveIngredientRepository.save(receiveIngredient);
    }

    private Pair<List<ReceiveIngredientResponse>, PaginationMetadata> getBySucceed(
            boolean succeed,
            Map<String, FilterCriteria> filters,
            Integer page,
            Integer size,
            String sortBy,
            String sortDir
    ) {
        List<Long> warehouseGroupIds = groupUtil.getAccessibleWarehouseGroupIds();
        List<Long> ingredientGroupIds = groupUtil.getAccessibleIngredientGroupIds();
        Pageable pageable = PageUtil.buildPageable(page, size, sortBy, sortDir);

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

        return getListPaginationMetadataPair(filters, sortBy, sortDir, pageable, spec);
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
        return getReceiveIngredientGroupResponse(groupId, groupExpenses, itemResponses);
    }

    private static @NonNull ReceiveIngredientGroupResponse getReceiveIngredientGroupResponse(Long groupId, List<ReceiveExpense> groupExpenses, List<ReceiveIngredientResponse> itemResponses) {
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
    public List<ReceiveIngredientResponse> update(
            Long groupId, List<ReceiveIngredientUpdateRequest> request,
            List<IngredientExpenseRequest> expenseRequests
    ) {
        List<ReceiveIngredient> existingGroupItems = extractGroupById(groupId, request);

        ReceiveGroup group = receiveGroupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Receive group not found"));

        boolean canReceiveUpdate = securityUtil.hasPermission("RECEIVE_INGREDIENT_STATUS_UPDATE");

        Map<Long, ReceiveIngredient> byId = existingGroupItems.stream()
                .collect(Collectors.toMap(ReceiveIngredient::getId, it -> it));

//        BigDecimal additionalExpense = expenseRequests == null ? BigDecimal.ZERO : expenseRequests.stream()
//                .map(IngredientExpenseRequest::getAmount)
//                .filter(Objects::nonNull)
//                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal receivedExpense = BigDecimal.ZERO;
        for (ReceiveIngredientUpdateRequest updateRequest : request) {
            if (updateRequest.getId() != null) {
                ReceiveIngredient current = byId.get(updateRequest.getId());
                BigDecimal price = updateRequest.getPrice() != null ? updateRequest.getPrice() : (current.getPrice() != null ? current.getPrice() : BigDecimal.ZERO);
                double qty = updateRequest.getQuantity() != null ? updateRequest.getQuantity() : (current.getQuantity() != null ? current.getQuantity() : 0.0);
                receivedExpense = receivedExpense.add(price.multiply(BigDecimal.valueOf(qty)));
            } else {
                if (updateRequest.getPrice() == null || updateRequest.getQuantity() == null) {
                    throw new InvalidDataException("New items must provide price and quantity");
                }
                receivedExpense = receivedExpense.add(updateRequest.getPrice().multiply(BigDecimal.valueOf(updateRequest.getQuantity())));
            }
        }

        List<ReceiveIngredientResponse> responses = new ArrayList<>();
        Set<Long> processedExistingIds = new HashSet<>();

        for (ReceiveIngredientUpdateRequest updateRequest : request) {
            if (updateRequest.getId() == null) {
                // TODO this validation should be handled via Spring Validator
                if (updateRequest.getIngredientId() == null)
                    throw new InvalidDataException("Ingredient is required for new item");
                if (updateRequest.getWarehouseId() == null)
                    throw new InvalidDataException("Warehouse is required for new item");
                if (updateRequest.getQuantity() == null)
                    throw new InvalidDataException("Quantity is required for new item");
                if (updateRequest.getPrice() == null) throw new InvalidDataException("Price is required for new item");

                ReceiveIngredient item = new ReceiveIngredient();
                ReceiveIngredient existingItem = extractReceiveIngredient(item, updateRequest);
                item.setCompanyId(updateRequest.getCompanyId());
                item.setImportDate(updateRequest.getImportDate());
                item.setManufacturingDate(updateRequest.getManufacturingDate());
                item.setGroup(group);
                item.setReceivedQuantity(0.0);
                if (canReceiveUpdate && updateRequest.getStatusId() != null) {
                    ReceiveIngredientStatus st = receiveIngredientStatusRepository.findById(updateRequest.getStatusId())
                            .orElseThrow(() -> new NotFoundException("Status not found"));
                    item.setStatus(st);
                } else {
                    item.setStatus(getStatusByName(STATUS_NOT_DELIVERED));
                }
                if (item.getManufacturingDate() != null && existingItem.getIngredient().getExpiration() != null) {
                    item.setExpirationDate(item.getManufacturingDate().plusDays(existingItem.getIngredient().getExpiration()));
                }
                ReceiveIngredient saved = receiveIngredientRepository.save(item);
                responses.add(receiveIngredientMapper.toSingleResponse(saved));
            } else {
                ReceiveIngredient item = byId.get(updateRequest.getId());
                if (!canReceiveUpdate && updateRequest.getStatusId() != null) {
                    throw new InvalidDataException("Only admin can update status");
                }

                Ingredient newIngredient = item.getIngredient();
                if (updateRequest.getIngredientId() != null && !Objects.equals(updateRequest.getIngredientId(), newIngredient != null ? newIngredient.getId() : null)) {
                    newIngredient = ingredientService.getIngredientById(updateRequest.getIngredientId());
                }

                Warehouse newWarehouse = item.getWarehouse();
                if (updateRequest.getWarehouseId() != null && !Objects.equals(updateRequest.getWarehouseId(), newWarehouse != null ? newWarehouse.getId() : null)) {
                    newWarehouse = warehouseService.getWarehouseById(updateRequest.getWarehouseId());
                }

                double oldQty = item.getQuantity() != null ? item.getQuantity() : 0.0;
                double newQty = updateRequest.getQuantity() != null ? updateRequest.getQuantity() : oldQty;

                BigDecimal basePrice = updateRequest.getPrice() != null ? updateRequest.getPrice() : (item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO);


                if (updateRequest.getImportDate() != null) {
                    item.setImportDate(updateRequest.getImportDate());
                }
                if (updateRequest.getManufacturingDate() != null) {
                    item.setManufacturingDate(updateRequest.getManufacturingDate());
                }

                Ingredient effectiveIngredient = newIngredient;
                if (item.getManufacturingDate() != null && effectiveIngredient != null && effectiveIngredient.getExpiration() != null) {
                    item.setExpirationDate(item.getManufacturingDate().plusDays(effectiveIngredient.getExpiration()));
                }

                if (updateRequest.getCompanyId() != null) {
                    item.setCompanyId(updateRequest.getCompanyId());
                }

                if (canReceiveUpdate && updateRequest.getStatusId() != null) {
                    ReceiveIngredientStatus st = receiveIngredientStatusRepository.findById(updateRequest.getStatusId())
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
            extractIngredientExpenseRequest(expenseRequests, group);
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
    public List<SelectResponse> getStatusesSelect() {
        return receiveIngredientStatusRepository.findAll().stream().map(s -> {
            SelectResponse response = new SelectResponse();
            response.setId(s.getId());
            response.setName(s.getName());
            return response;
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ReceiveIngredientsPriceCalcResponse calcIngredientPrices(List<ReceiveIngredientsPriceCalcRequest> ingredients, List<IngredientExpenseRequest> expenses) {
        if (ingredients == null || ingredients.isEmpty()) {
            throw new InvalidDataException("Ingredients list cannot be empty");
        }

        BigDecimal totalBase = BigDecimal.ZERO;
        BigDecimal totalWithoutExpense;
        for (ReceiveIngredientsPriceCalcRequest request : ingredients) {
            if (request.getIngredientId() == null) throw new InvalidDataException("Ingredient id is required");
            BigDecimal price = request.getPrice() != null ? request.getPrice() : BigDecimal.ZERO;
            double qty = request.getQuantity() != null ? request.getQuantity() : 0.0;
            totalBase = totalBase.add(price.multiply(BigDecimal.valueOf(qty)));
        }
        BigDecimal totalExpenses = (expenses == null ? new ArrayList<IngredientExpenseRequest>() : expenses)
                .stream()
                .map(IngredientExpenseRequest::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        totalWithoutExpense = totalBase;

        ReceiveIngredientsPriceCalcResponse response = new ReceiveIngredientsPriceCalcResponse();
        List<ReceiveIngredientsPriceCalcResponse.Ingredients> ingredientResponses = new ArrayList<>();

        for (ReceiveIngredientsPriceCalcRequest receiveIngredientsPriceCalcRequest : ingredients) {
            Ingredient ingredient = ingredientService.getIngredientById(receiveIngredientsPriceCalcRequest.getIngredientId());

            BigDecimal price = receiveIngredientsPriceCalcRequest.getPrice() != null
                    ? receiveIngredientsPriceCalcRequest.getPrice()
                    : BigDecimal.ZERO;
            double qty = receiveIngredientsPriceCalcRequest.getQuantity() != null
                    ? receiveIngredientsPriceCalcRequest.getQuantity()
                    : 0.0;

//            BigDecimal itemBaseAmount = price.multiply(BigDecimal.valueOf(qty));
//            BigDecimal additionalPerUnit = BigDecimal.ZERO;
//            if (totalExpenses.compareTo(BigDecimal.ZERO) > 0
//                    && totalBase.compareTo(BigDecimal.ZERO) > 0
//                    && qty > 0.0) {
//                additionalPerUnit = itemBaseAmount
//                        .divide(totalBase, 8, RoundingMode.HALF_EVEN)
//                        .multiply(totalExpenses)
//                        .divide(BigDecimal.valueOf(qty), 2, RoundingMode.HALF_EVEN);
//            }
//            BigDecimal calculatedPrice = price.add(additionalPerUnit);
//            BigDecimal total = calculatedPrice.multiply(BigDecimal.valueOf(qty)).setScale(2, RoundingMode.HALF_EVEN);

            IngredientPriceRecord ingredientPriceUtil =
                    IngredientPriceUtil.calculateIngredientPrice(price, qty, totalExpenses, totalBase);

//            ReceiveIngredientsPriceCalcResponse.Ingredients receiveIngredientsPriceCalcResponse = new ReceiveIngredientsPriceCalcResponse.Ingredients();
//            receiveIngredientsPriceCalcResponse.setIngredientId(ingredient.getId());
//            receiveIngredientsPriceCalcResponse.setIngredientName(ingredient.getName());
//            receiveIngredientsPriceCalcResponse.setUnitName(ingredient.getUnit().getName());
//            receiveIngredientsPriceCalcResponse.setQuantity(qty);
//            receiveIngredientsPriceCalcResponse.setPrice(price.setScale(2, RoundingMode.HALF_EVEN));
//            receiveIngredientsPriceCalcResponse.setCalculatedPrice(calculatedPrice.setScale(2, RoundingMode.HALF_EVEN));
//            receiveIngredientsPriceCalcResponse.setTotal(total);
//            ingredientResponses.add(receiveIngredientsPriceCalcResponse);
            ReceiveIngredientsPriceCalcResponse.Ingredients receiveIngredientsPriceCalcResponse =
                    receiveIngredientMapper.toIngredientResponse(ingredient, qty, price, ingredientPriceUtil.calculatedPrice(), ingredientPriceUtil.total());
            ingredientResponses.add(receiveIngredientsPriceCalcResponse);
        }

        response.setIngredients(ingredientResponses);
        response.setPriceWithoutExpense(totalWithoutExpense);
        response.setTotalPrice(totalBase.add(totalExpenses));

        List<ReceiveIngredientsPriceCalcResponse.Expenses> expenseResponses = new ArrayList<>();
        if (expenses != null) {
            for (IngredientExpenseRequest ingredientExpenseRequest : expenses) {
                if (ingredientExpenseRequest.getExpenseTypeId() == null) continue;
//                ExpenseType et = expenseTypeRepository.findById(ingredientExpenseRequest.getExpenseTypeId())
//                        .orElseThrow(() -> new NotFoundException("Expense type not found"));
                ExpenseType expenseType = expenseTypeService.getExpenseTypeById(ingredientExpenseRequest.getExpenseTypeId());

                ReceiveIngredientsPriceCalcResponse.Expenses expenseDetails = new ReceiveIngredientsPriceCalcResponse.Expenses();
                expenseDetails.setExpenseTypeName(expenseType.getName());
                expenseDetails.setAmount(ingredientExpenseRequest.getAmount() != null
                        ? ingredientExpenseRequest.getAmount().setScale(2, RoundingMode.HALF_EVEN)
                        : BigDecimal.ZERO
                );
                expenseResponses.add(expenseDetails);
            }
        }
        response.setExpenses(expenseResponses);

        return response;
    }

    @NonNull
    private Pair<List<ReceiveIngredientResponse>, PaginationMetadata> getListPaginationMetadataPair(Map<String, FilterCriteria> filters, String sortBy, String sortDir, Pageable pageable, Specification<ReceiveIngredient> spec) {
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

    private ReceiveIngredient extractReceiveIngredient(ReceiveIngredient item, ReceiveIngredientUpdateRequest request) {

//        Ingredient ingredient = ingredientRepository.findById(request.getIngredientId())
//                .orElseThrow(() -> new NotFoundException("Ingredient not found"));
//        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
//                .orElseThrow(() -> new NotFoundException("Warehouse not found"));

        Ingredient ingredient = ingredientService.getIngredientById(request.getIngredientId());
        Warehouse warehouse = warehouseService.getWarehouseById(request.getWarehouseId());

        double newQty = request.getQuantity();
        BigDecimal basePrice = request.getPrice();
        item.setIngredient(ingredient);
        item.setWarehouse(warehouse);
        item.setQuantity(newQty);
        item.setPrice(basePrice);
        return item;
    }

    private List<ReceiveIngredient> extractGroupById(Long groupId, List<?> request) {
        List<ReceiveIngredient> existingGroupItems = receiveIngredientRepository.findByGroupId(groupId);
        if (existingGroupItems == null || existingGroupItems.isEmpty()) {
            throw new NotFoundException("Receive group not found");
        }
        if (request == null || request.isEmpty()) {
            throw new InvalidDataException("Update request cannot be empty");
        }
        return existingGroupItems;
    }

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
        } else {
            item.setStatus(getStatusByName(STATUS_PRICE_MISMATCH));
        }
    }

    private boolean isCompleted(ReceiveIngredient item) {
        ReceiveIngredientStatus ingredientStatus = item.getStatus();
        return ingredientStatus != null && STATUS_COMPLETED.equals(ingredientStatus.getName());
    }
}