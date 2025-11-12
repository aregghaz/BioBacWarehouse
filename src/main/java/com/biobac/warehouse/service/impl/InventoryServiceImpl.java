package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.dto.ChangeComponentDto;
import com.biobac.warehouse.dto.IngredientHistoryDto;
import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.dto.ProductHistoryDto;
import com.biobac.warehouse.entity.*;
import com.biobac.warehouse.exception.NotFoundException;
import com.biobac.warehouse.repository.*;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.InventoryResponse;
import com.biobac.warehouse.service.IngredientHistoryService;
import com.biobac.warehouse.service.InventoryService;
import com.biobac.warehouse.service.ProductHistoryService;
import com.biobac.warehouse.utils.specifications.InventorySpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {
    private final IngredientBalanceRepository ingredientBalanceRepository;
    private final ProductBalanceRepository productBalanceRepository;
    private final WarehouseRepository warehouseRepository;
    private final IngredientRepository ingredientRepository;
    private final ProductRepository productRepository;
    private final IngredientHistoryService ingredientHistoryService;
    private final ProductHistoryService productHistoryService;
    private final IngredientDetailRepository ingredientDetailRepository;
    private final ProductDetailRepository productDetailRepository;
    private final InventoryRepository inventoryRepository;

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final String DEFAULT_SORT_BY = "date";
    private static final String DEFAULT_SORT_DIR = "desc";
    private final HistoryActionRepository historyActionRepository;

    private Pageable buildPageable(Integer page, Integer size, String sortBy, String sortDir) {
        int safePage = (page == null || page < 0) ? DEFAULT_PAGE : page;
        int safeSize = (size == null || size <= 0) ? DEFAULT_SIZE : size;
        if (safeSize > 1000) safeSize = 1000;

        String safeSortBy = (sortBy == null || sortBy.isBlank()) ? DEFAULT_SORT_BY : sortBy.trim();
        String safeSortDir = (sortDir == null || sortDir.isBlank()) ? DEFAULT_SORT_DIR : sortDir.trim();

        Sort sort = safeSortDir.equalsIgnoreCase("asc")
                ? Sort.by(safeSortBy).ascending()
                : Sort.by(safeSortBy).descending();

        return PageRequest.of(safePage, safeSize, sort);
    }

    @Override
    @Transactional
    public void changeProduct(List<ChangeComponentDto> request) {
        if (request == null || request.isEmpty()) return;

        for (ChangeComponentDto c : request) {
            Product product = productRepository.findById(c.getComponentId())
                    .orElseThrow(() -> new NotFoundException("Product not found"));
            Warehouse warehouse = warehouseRepository.findById(c.getWarehouseId())
                    .orElseThrow(() -> new NotFoundException("Warehouse not found"));

            ProductBalance balance = productBalanceRepository
                    .findByWarehouseAndProduct(warehouse, product)
                    .orElseGet(() -> {
                        ProductBalance newBalance = new ProductBalance();
                        newBalance.setBalance(0.0);
                        newBalance.setWarehouse(warehouse);
                        newBalance.setProduct(product);
                        return productBalanceRepository.save(newBalance);
                    });

            balance.setBalance(balance.getBalance() + c.getQuantity());

            productBalanceRepository.save(balance);

            Inventory inventory = new Inventory();
            inventory.setComponentId(c.getComponentId());
            inventory.setWarehouse(warehouse);
            inventory.setType(ComponentType.PRODUCT);
            inventory.setDate(c.getDate());
            inventory.setQuantity(c.getQuantity());
            inventoryRepository.save(inventory);

            ProductHistoryDto dto = new ProductHistoryDto();
            HistoryAction action = historyActionRepository.findById(2L)
                    .orElseThrow(() -> new NotFoundException("Action not found"));
            dto.setProduct(product);
            dto.setWarehouse(warehouse);
            dto.setTimestamp(c.getDate());
            dto.setQuantityChange(c.getQuantity());
            dto.setQuantityResult(balance.getBalance());
            dto.setNotes("");
            dto.setAction(action);
            productHistoryService.recordQuantityChange(dto);
        }
    }

    @Override
    @Transactional
    public void changeIngredient(List<ChangeComponentDto> request) {
        if (request == null || request.isEmpty()) return;

        for (ChangeComponentDto c : request) {
            Ingredient ingredient = ingredientRepository.findById(c.getComponentId())
                    .orElseThrow(() -> new NotFoundException("Ingredient not found"));
            Warehouse warehouse = warehouseRepository.findById(c.getWarehouseId())
                    .orElseThrow(() -> new NotFoundException("Warehouse not found"));

            IngredientBalance balance = ingredientBalanceRepository
                    .findByWarehouseAndIngredient(warehouse, ingredient)
                    .orElseGet(() -> {
                        IngredientBalance newBalance = new IngredientBalance();
                        newBalance.setBalance(0.0);
                        newBalance.setWarehouse(warehouse);
                        newBalance.setIngredient(ingredient);
                        return ingredientBalanceRepository.save(newBalance);
                    });

            balance.setBalance(balance.getBalance() + c.getQuantity());


            ingredientBalanceRepository.save(balance);

            Inventory inventory = new Inventory();
            inventory.setComponentId(c.getComponentId());
            inventory.setWarehouse(warehouse);
            inventory.setType(ComponentType.INGREDIENT);
            inventory.setDate(c.getDate());
            inventory.setQuantity(c.getQuantity());
            inventoryRepository.save(inventory);

            IngredientHistoryDto dto = new IngredientHistoryDto();
            HistoryAction action = historyActionRepository.findById(2L)
                    .orElseThrow(() -> new NotFoundException("Action not found"));
            dto.setIngredient(ingredient);
            dto.setWarehouse(warehouse);
            dto.setTimestamp(c.getDate());
            dto.setQuantityChange(c.getQuantity());
            dto.setQuantityResult(balance.getBalance());
            dto.setNotes("");
            dto.setAction(action);
            ingredientHistoryService.recordQuantityChange(dto);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<InventoryResponse>, PaginationMetadata> getPagination(
            Map<String, FilterCriteria> filters,
            Integer page,
            Integer size,
            String sortBy,
            String sortDir,
            ComponentType type
    ) {
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);

        Specification<Inventory> spec = Specification
                .where(InventorySpecification.hasType(type))
                .and(InventorySpecification.buildSpecification(filters, type));

        Page<Inventory> inventoryPage = inventoryRepository.findAll(spec, pageable);

        List<InventoryResponse> content = mapInventoriesToResponses(inventoryPage.getContent(), type);
        PaginationMetadata metadata = buildPaginationMetadata(inventoryPage, filters, pageable);

        return Pair.of(content, metadata);
    }

    private List<InventoryResponse> mapInventoriesToResponses(List<Inventory> inventories, ComponentType type) {
        List<InventoryResponse> responses = new ArrayList<>();
        for (Inventory i : inventories) {
            InventoryResponse r = new InventoryResponse();
            r.setDate(i.getDate());
            r.setWarehouseName(i.getWarehouse() != null ? i.getWarehouse().getName() : null);
            r.setQuantity(i.getQuantity());
            r.setComponentName(fetchComponentName(i.getComponentId(), type));
            responses.add(r);
        }
        return responses;
    }

    private String fetchComponentName(Long componentId, ComponentType type) {
        if (componentId == null) return null;
        return switch (type) {
            case INGREDIENT -> ingredientRepository.findById(componentId)
                    .map(Ingredient::getName)
                    .orElse(null);
            case PRODUCT -> productRepository.findById(componentId)
                    .map(Product::getName)
                    .orElse(null);
            default -> null;
        };
    }

    private <T> PaginationMetadata buildPaginationMetadata(Page<T> page, Map<String, FilterCriteria> filters, Pageable pageable) {
        return new PaginationMetadata(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast(),
                filters,
                pageable.getSort().toString().contains("ASC") ? "asc" : "desc",
                pageable.getSort().stream()
                        .findFirst()
                        .map(Sort.Order::getProperty)
                        .orElse(DEFAULT_SORT_BY),
                "inventoryTable"
        );
    }
}
