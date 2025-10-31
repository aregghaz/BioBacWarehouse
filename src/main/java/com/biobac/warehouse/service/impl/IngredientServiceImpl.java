package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.client.AttributeClient;
import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.entity.*;
import com.biobac.warehouse.exception.InvalidDataException;
import com.biobac.warehouse.exception.NotFoundException;
import com.biobac.warehouse.mapper.IngredientMapper;
import com.biobac.warehouse.repository.*;
import com.biobac.warehouse.request.*;
import com.biobac.warehouse.response.IngredientResponse;
import com.biobac.warehouse.response.UnitTypeCalculatedResponse;
import com.biobac.warehouse.service.IngredientHistoryService;
import com.biobac.warehouse.service.IngredientService;
import com.biobac.warehouse.service.UnitTypeCalculator;
import com.biobac.warehouse.utils.GroupUtil;
import com.biobac.warehouse.utils.specifications.IngredientSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngredientServiceImpl implements IngredientService, UnitTypeCalculator {
    private final IngredientRepository ingredientRepository;
    private final IngredientGroupRepository ingredientGroupRepository;
    private final WarehouseRepository warehouseRepository;
    private final UnitRepository unitRepository;
    private final UnitTypeRepository unitTypeRepository;
    private final IngredientHistoryService ingredientHistoryService;
    private final IngredientMapper ingredientMapper;
    private final AttributeClient attributeClient;
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
            case "ingredientGroupName" -> "ingredientGroup.name";
            case "unitName" -> "unit.name";
            default -> sortBy;
        };
    }

    @Override
    @Transactional
    public IngredientResponse create(IngredientCreateRequest request) {
        Ingredient ingredient = new Ingredient();
        ingredient.setName(request.getName());
        ingredient.setDescription(request.getDescription());
        ingredient.setPrice(request.getPrice());
        ingredient.setMinimalBalance(request.getMinimalBalance() != null ? request.getMinimalBalance() : 0);
        if (request.getExpiration() != null) {
            ingredient.setExpiration(request.getExpiration());
        }

        if (request.getIngredientGroupId() != null) {
            IngredientGroup ingredientGroup = ingredientGroupRepository.findById(request.getIngredientGroupId())
                    .orElseThrow(() -> new NotFoundException("Ingredient group not found"));
            ingredient.setIngredientGroup(ingredientGroup);
        }

        if (request.getUnitId() != null) {
            Unit unit = unitRepository.findById(request.getUnitId())
                    .orElseThrow(() -> new NotFoundException("Unit not found"));
            ingredient.setUnit(unit);
        }

        if (request.getDefaultWarehouseId() != null) {
            Warehouse warehouse = warehouseRepository.findById(request.getDefaultWarehouseId())
                    .orElseThrow(() -> new NotFoundException("Warehouse not found"));
            ingredient.setDefaultWarehouse(warehouse);
        }

        if (ingredient.getUnit() != null) {
            Unit unit = ingredient.getUnit();

            UnitType baseUnitType = unitTypeRepository.findByName(unit.getName())
                    .orElseGet(() -> {
                        UnitType newType = new UnitType();
                        newType.setName(unit.getName());
                        return unitTypeRepository.save(newType);
                    });

            boolean alreadyExists = ingredient.getUnitTypeConfigs().stream()
                    .anyMatch(link -> link.getUnitType().equals(baseUnitType));

            if (!alreadyExists) {
                IngredientUnitType baseLink = new IngredientUnitType();
                baseLink.setIngredient(ingredient);
                baseLink.setUnitType(baseUnitType);
                baseLink.setSize(1.0);
                baseLink.setBaseType(true);
                ingredient.getUnitTypeConfigs().add(baseLink);
            }
        }

        if (request.getUnitTypeConfigs() != null) {
            Set<UnitType> allowedTypes = ingredient.getUnit() != null && ingredient.getUnit().getUnitTypes() != null
                    ? ingredient.getUnit().getUnitTypes() : new HashSet<>();
            for (UnitTypeConfigRequest cfgReq : request.getUnitTypeConfigs()) {
                if (cfgReq.getUnitTypeId() == null) {
                    throw new InvalidDataException("unitTypeId is required in unitTypeConfigs");
                }
                UnitType ut = unitTypeRepository.findById(cfgReq.getUnitTypeId())
                        .orElseThrow(() -> new NotFoundException("UnitType not found"));
                if (!allowedTypes.isEmpty() && !allowedTypes.contains(ut)) {
                    throw new InvalidDataException("UnitType '" + ut.getName() + "' is not allowed for selected Unit");
                }
                IngredientUnitType link = new IngredientUnitType();
                link.setIngredient(ingredient);
                link.setUnitType(ut);
                link.setBaseType(false);
                link.setSize(cfgReq.getSize());
                ingredient.getUnitTypeConfigs().add(link);
            }
        }

        Ingredient saved = ingredientRepository.save(ingredient);

        if (request.getAttributes() != null && !request.getAttributes().isEmpty()) {
            attributeClient.createValues(saved.getId(), AttributeTargetType.INGREDIENT.name(), request.getAttributes());
        }

        if (request.getAttributeGroupIds() != null && !request.getAttributeGroupIds().isEmpty()) {
            saved.setAttributeGroupIds(request.getAttributeGroupIds());
        }

        return ingredientMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public IngredientResponse getById(Long id) {
        Ingredient ingredient = ingredientRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Ingredient not found"));
        return ingredientMapper.toResponse(ingredient);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IngredientResponse> getAll() {

        List<Ingredient> ingredients = ingredientRepository.findAllByDeletedFalse();

        List<Long> groupIds = groupUtil.getAccessibleWarehouseGroupIds();

        return ingredients.stream()
                .map(ingredientMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public IngredientResponse update(Long id, IngredientUpdateRequest request) {
        Ingredient existing = ingredientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ingredient not found"));

        if (request.getName() != null) {
            existing.setName(request.getName());
        }
        if (request.getDescription() != null) {
            existing.setDescription(request.getDescription());
        }

        if (request.getPrice() != null) {
            existing.setPrice(request.getPrice());
        }

        if (request.getMinimalBalance() != null) {
            existing.setMinimalBalance(request.getMinimalBalance());
        }

        if (request.getExpiration() != null) {
            existing.setExpiration(request.getExpiration());
        }

        if (request.getIngredientGroupId() != null) {
            IngredientGroup ingredientGroup = ingredientGroupRepository.findById(request.getIngredientGroupId())
                    .orElseThrow(() -> new NotFoundException("Ingredient group not found"));
            existing.setIngredientGroup(ingredientGroup);
        }

        if (request.getUnitId() != null) {
            Unit unit = unitRepository.findById(request.getUnitId())
                    .orElseThrow(() -> new NotFoundException("Unit not found"));
            existing.setUnit(unit);
        }

        if (request.getDefaultWarehouseId() != null) {
            Warehouse warehouse = warehouseRepository.findById(request.getDefaultWarehouseId())
                    .orElseThrow(() -> new NotFoundException("Warehouse not found"));
            existing.setDefaultWarehouse(warehouse);
        }

        if (request.getUnitTypeConfigs() != null) {
            Set<UnitType> allowedTypes = existing.getUnit() != null && existing.getUnit().getUnitTypes() != null
                    ? existing.getUnit().getUnitTypes() : new HashSet<>();

            existing.getUnitTypeConfigs().clear();

            UnitType baseUnitType = null;
            if (existing.getUnit() != null) {
                Unit unit = existing.getUnit();
                baseUnitType = unitTypeRepository.findByName(unit.getName())
                        .orElseGet(() -> {
                            UnitType newType = new UnitType();
                            newType.setName(unit.getName());
                            return unitTypeRepository.save(newType);
                        });
                IngredientUnitType baseLink = new IngredientUnitType();
                baseLink.setIngredient(existing);
                baseLink.setUnitType(baseUnitType);
                baseLink.setSize(1.0);
                baseLink.setBaseType(true);
                existing.getUnitTypeConfigs().add(baseLink);
            }

            for (UnitTypeConfigRequest cfgReq : request.getUnitTypeConfigs()) {
                if (cfgReq.getUnitTypeId() == null) {
                    throw new InvalidDataException("unitTypeId is required in unitTypeConfigs");
                }
                UnitType ut = unitTypeRepository.findById(cfgReq.getUnitTypeId())
                        .orElseThrow(() -> new NotFoundException("UnitType not found"));

                if (baseUnitType != null && ut.equals(baseUnitType)) {
                    continue;
                }

                if (!allowedTypes.isEmpty() && !allowedTypes.contains(ut)) {
                    throw new InvalidDataException("UnitType '" + ut.getName() + "' is not allowed for selected Unit");
                }
                IngredientUnitType link = new IngredientUnitType();
                link.setIngredient(existing);
                link.setUnitType(ut);
                link.setSize(cfgReq.getSize());
                link.setBaseType(false);
                existing.getUnitTypeConfigs().add(link);
            }
        }

        if (request.getAttributeGroupIds() != null) {
            existing.setAttributeGroupIds(request.getAttributeGroupIds());
        }

        Ingredient saved = ingredientRepository.save(existing);

        List<AttributeUpsertRequest> attributes = request.getAttributeGroupIds() == null || request.getAttributeGroupIds().isEmpty() ? Collections.emptyList() : request.getAttributes();

        attributeClient.updateValues(saved.getId(), AttributeTargetType.INGREDIENT.name(), request.getAttributeGroupIds(), attributes);

        return ingredientMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<IngredientResponse>, PaginationMetadata> getPagination(Map<String, FilterCriteria> filters,
                                                                            Integer page,
                                                                            Integer size,
                                                                            String sortBy,
                                                                            String sortDir) {
        List<Long> groupIds = groupUtil.getAccessibleWarehouseGroupIds();
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);
        Specification<Ingredient> spec = IngredientSpecification.buildSpecification(filters)
                .and(IngredientSpecification.belongsToGroups(groupIds));
        Page<Ingredient> ingredientPage = ingredientRepository.findAll(spec, pageable);

        List<IngredientResponse> content = ingredientPage.getContent()
                .stream()
                .map(ingredientMapper::toResponse)
                .collect(Collectors.toList());

        PaginationMetadata metadata = new PaginationMetadata(
                ingredientPage.getNumber(),
                ingredientPage.getSize(),
                ingredientPage.getTotalElements(),
                ingredientPage.getTotalPages(),
                ingredientPage.isLast(),
                filters,
                pageable.getSort().toString().contains("ASC") ? "asc" : "desc",
                pageable.getSort().stream().findFirst().map(Sort.Order::getProperty).orElse(DEFAULT_SORT_BY),
                "ingredientTable"
        );

        return Pair.of(content, metadata);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ingredient not found"));

        attributeClient.deleteValues(id, AttributeTargetType.INGREDIENT.name());

        double totalBefore = 0.0;

        ingredient.setDeleted(true);
        ingredientRepository.save(ingredient);

        ingredientHistoryService.recordQuantityChange(LocalDate.now(), ingredient, totalBefore, 0.0, "ingredient deleted", null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnitTypeCalculatedResponse> calculateUnitTypes(Long id, InventoryUnitTypeRequest request) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ingredient not found"));

        IngredientUnitType config = ingredient.getUnitTypeConfigs().stream()
                .filter(c -> c.getId().equals(request.getId()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Unit Type Config not found for this ingredient"));

        double total = config.isBaseType() ? request.getCount() : config.getSize() * request.getCount();

        return ingredient.getUnitTypeConfigs().stream().map(utc -> {
            UnitTypeCalculatedResponse calculatedResponse = new UnitTypeCalculatedResponse();
            calculatedResponse.setUnitTypeName(utc.getUnitType().getName());
            calculatedResponse.setUnitTypeId(utc.getId());
            calculatedResponse.setBaseUnit(utc.isBaseType());
            if (utc.isBaseType()) {
                calculatedResponse.setSize(total);
            } else {
                calculatedResponse.setSize(Math.ceil(total / utc.getSize()));
            }
            return calculatedResponse;
        }).toList();
    }
}