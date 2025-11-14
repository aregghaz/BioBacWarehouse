package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.dto.IngredientHistoryDto;
import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.dto.ProductHistoryDto;
import com.biobac.warehouse.dto.TransferComponentDto;
import com.biobac.warehouse.entity.*;
import com.biobac.warehouse.exception.NotFoundException;
import com.biobac.warehouse.repository.*;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.TransferResponse;
import com.biobac.warehouse.service.IngredientHistoryService;
import com.biobac.warehouse.service.ProductHistoryService;
import com.biobac.warehouse.service.TransferService;
import com.biobac.warehouse.utils.specifications.TransferSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {
    private final TransferRepository transferRepository;
    private final IngredientBalanceRepository ingredientBalanceRepository;
    private final ProductBalanceRepository productBalanceRepository;
    private final WarehouseRepository warehouseRepository;
    private final IngredientRepository ingredientRepository;
    private final ProductRepository productRepository;
    private final IngredientDetailRepository ingredientDetailRepository;
    private final ProductDetailRepository productDetailRepository;
    private final IngredientHistoryService ingredientHistoryService;
    private final ProductHistoryService productHistoryService;
    private final HistoryActionRepository historyActionRepository;

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final String DEFAULT_SORT_BY = "date";
    private static final String DEFAULT_SORT_DIR = "desc";

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
    public void transferProduct(List<TransferComponentDto> request) {
        if (request == null || request.isEmpty()) return;

        for (TransferComponentDto c : request) {
            Product product = productRepository.findById(c.getComponentId())
                    .orElseThrow(() -> new NotFoundException("Product not found"));
            Warehouse from = warehouseRepository.findById(c.getFromWarehouseId())
                    .orElseThrow(() -> new NotFoundException("Warehouse not found"));
            Warehouse to = warehouseRepository.findById(c.getToWarehouseId())
                    .orElseThrow(() -> new NotFoundException("Warehouse not found"));

            ProductBalance fromBalance = productBalanceRepository.findByWarehouseAndProduct(from, product)
                    .orElseThrow(() -> new NotFoundException("Product not found on that warehouse"));

            ProductBalance toBalance = productBalanceRepository
                    .findByWarehouseAndProduct(to, product)
                    .orElseGet(() -> {
                        ProductBalance newBalance = new ProductBalance();
                        newBalance.setBalance(0.0);
                        newBalance.setWarehouse(to);
                        newBalance.setProduct(product);
                        return productBalanceRepository.save(newBalance);
                    });

            double requiredQty = Optional.ofNullable(c.getQuantity()).orElse(0.0);
            if (requiredQty <= 0) continue;

            fromBalance.setBalance(Optional.ofNullable(fromBalance.getBalance()).orElse(0.0) - requiredQty);
            toBalance.setBalance(Optional.ofNullable(toBalance.getBalance()).orElse(0.0) + requiredQty);
            productBalanceRepository.saveAll(List.of(fromBalance, toBalance));

            List<ProductDetail> fromDetails = productDetailRepository
                    .findByProductBalanceIdOrderByExpirationDateAsc(fromBalance.getId());

            double remaining = requiredQty;

            for (ProductDetail fromDetail : fromDetails) {
                if (remaining <= 0) break;

                double available = Optional.ofNullable(fromDetail.getQuantity()).orElse(0.0);
                double toTransfer = Math.min(available, remaining);
                if (toTransfer <= 0) continue;

                fromDetail.setQuantity(available - toTransfer);
                productDetailRepository.save(fromDetail);

                ProductDetail toDetail = productDetailRepository
                        .findByProductBalanceIdAndExpirationDate(toBalance.getId(), fromDetail.getExpirationDate())
                        .orElseGet(() -> {
                            ProductDetail newDetail = new ProductDetail();
                            newDetail.setProductBalance(toBalance);
                            newDetail.setExpirationDate(fromDetail.getExpirationDate());
                            newDetail.setPrice(fromDetail.getPrice());
                            newDetail.setManufacturingDate(fromDetail.getManufacturingDate());
                            newDetail.setQuantity(0.0);
                            return newDetail;
                        });

                toDetail.setProductBalance(toBalance);
                toDetail.setQuantity(Optional.ofNullable(toDetail.getQuantity()).orElse(0.0) + toTransfer);
                productDetailRepository.save(toDetail);

                remaining -= toTransfer;
            }

            Transfer t = new Transfer();
            t.setType(ComponentType.PRODUCT);
            t.setComponentId(product.getId());
            t.setFrom(from);
            t.setTo(to);
            t.setDate(Optional.ofNullable(c.getDate()).orElse(LocalDateTime.now()));
            t.setQuantity(requiredQty);
            transferRepository.save(t);

            HistoryAction action = historyActionRepository.findById(1L)
                    .orElseThrow(() -> new NotFoundException("Action not found"));
            String productName = product.getName() != null ? product.getName() : ("#" + product.getId());
            String fromWhName = from.getName() != null ? from.getName() : ("#" + from.getId());
            String toWhName = to.getName() != null ? to.getName() : ("#" + to.getId());

            String fromNote = String.format(
                    "Перемещено %.2f единиц продукта \"%s\" со склада \"%s\" на склад \"%s\" (списание)",
                    requiredQty, productName, fromWhName, toWhName
            );

            String toNote = String.format(
                    "Получено %.2f единиц продукта \"%s\" со склада \"%s\" на склад \"%s\" (поступление)",
                    requiredQty, productName, fromWhName, toWhName
            );

            ProductHistoryDto dtoFrom = new ProductHistoryDto();
            dtoFrom.setProduct(product);
            dtoFrom.setWarehouse(from);
            dtoFrom.setTimestamp(c.getDate());
            dtoFrom.setQuantityChange(-requiredQty);
            dtoFrom.setNotes(fromNote);
            dtoFrom.setAction(action);
            productHistoryService.recordQuantityChange(dtoFrom);

            ProductHistoryDto dtoTo = new ProductHistoryDto();
            dtoTo.setProduct(product);
            dtoTo.setWarehouse(to);
            dtoTo.setTimestamp(c.getDate());
            dtoTo.setQuantityChange(requiredQty);
            dtoTo.setNotes(toNote);
            dtoTo.setAction(action);
            productHistoryService.recordQuantityChange(dtoTo);
        }
    }

    @Override
    @Transactional
    public void transferIngredient(List<TransferComponentDto> request) {
        if (request == null || request.isEmpty()) return;

        for (TransferComponentDto c : request) {
            Ingredient ingredient = ingredientRepository.findById(c.getComponentId())
                    .orElseThrow(() -> new NotFoundException("Ingredient not found"));
            Warehouse from = warehouseRepository.findById(c.getFromWarehouseId())
                    .orElseThrow(() -> new NotFoundException("Warehouse not found"));
            Warehouse to = warehouseRepository.findById(c.getToWarehouseId())
                    .orElseThrow(() -> new NotFoundException("Warehouse not found"));

            IngredientBalance fromBalance = ingredientBalanceRepository.findByWarehouseAndIngredient(from, ingredient)
                    .orElseThrow(() -> new NotFoundException("Ingredient not found on that warehouse"));

            IngredientBalance toBalance = ingredientBalanceRepository
                    .findByWarehouseAndIngredient(to, ingredient)
                    .orElseGet(() -> {
                        IngredientBalance newBalance = new IngredientBalance();
                        newBalance.setBalance(0.0);
                        newBalance.setWarehouse(to);
                        newBalance.setIngredient(ingredient);
                        return ingredientBalanceRepository.save(newBalance);
                    });

            double requiredQty = Optional.ofNullable(c.getQuantity()).orElse(0.0);
            if (requiredQty <= 0) continue;

            fromBalance.setBalance(Optional.ofNullable(fromBalance.getBalance()).orElse(0.0) - requiredQty);
            toBalance.setBalance(Optional.ofNullable(toBalance.getBalance()).orElse(0.0) + requiredQty);
            ingredientBalanceRepository.saveAll(List.of(fromBalance, toBalance));

            List<IngredientDetail> fromDetails = ingredientDetailRepository
                    .findByIngredientBalanceIdOrderByExpirationDateAsc(fromBalance.getId());

            double remaining = requiredQty;

            for (IngredientDetail fromDetail : fromDetails) {
                if (remaining <= 0) break;

                double available = Optional.ofNullable(fromDetail.getQuantity()).orElse(0.0);
                double toTransfer = Math.min(available, remaining);
                if (toTransfer <= 0) continue;

                fromDetail.setQuantity(available - toTransfer);
                ingredientDetailRepository.save(fromDetail);

                IngredientDetail toDetail = ingredientDetailRepository
                        .findByIngredientBalanceIdAndExpirationDate(toBalance.getId(), fromDetail.getExpirationDate())
                        .orElseGet(() -> {
                            IngredientDetail newDetail = new IngredientDetail();
                            newDetail.setIngredientBalance(toBalance);
                            newDetail.setPrice(fromDetail.getPrice());
                            newDetail.setImportDate(fromDetail.getImportDate());
                            newDetail.setManufacturingDate(fromDetail.getManufacturingDate());
                            newDetail.setExpirationDate(fromDetail.getExpirationDate());
                            newDetail.setQuantity(0.0);
                            return newDetail;
                        });

                toDetail.setIngredientBalance(toBalance);
                toDetail.setQuantity(Optional.ofNullable(toDetail.getQuantity()).orElse(0.0) + toTransfer);
                ingredientDetailRepository.save(toDetail);

                remaining -= toTransfer;
            }

            Transfer t = new Transfer();
            t.setType(ComponentType.INGREDIENT);
            t.setComponentId(ingredient.getId());
            t.setFrom(from);
            t.setTo(to);
            t.setDate(Optional.ofNullable(c.getDate()).orElse(LocalDateTime.now()));
            t.setQuantity(requiredQty);
            transferRepository.save(t);

            HistoryAction action = historyActionRepository.findById(1L)
                    .orElseThrow(() -> new NotFoundException("Action not found"));

            String ingredientName = ingredient.getName() != null ? ingredient.getName() : ("#" + ingredient.getId());
            String fromWhName = from.getName() != null ? from.getName() : ("#" + from.getId());
            String toWhName = to.getName() != null ? to.getName() : ("#" + to.getId());

            String fromNote = String.format(
                    "Перемещено %.2f единиц ингредиента \"%s\" со склада \"%s\" на склад \"%s\" (списание)",
                    c.getQuantity(), ingredientName, fromWhName, toWhName
            );

            String toNote = String.format(
                    "Получено %.2f единиц ингредиента \"%s\" со склада \"%s\" на склад \"%s\" (поступление)",
                    c.getQuantity(), ingredientName, fromWhName, toWhName
            );

            IngredientHistoryDto dtoFrom = new IngredientHistoryDto();
            dtoFrom.setIngredient(ingredient);
            dtoFrom.setWarehouse(from);
            dtoFrom.setTimestamp(c.getDate());
            dtoFrom.setQuantityChange(-c.getQuantity());
            dtoFrom.setNotes(fromNote);
            dtoFrom.setAction(action);
            ingredientHistoryService.recordQuantityChange(dtoFrom);

            IngredientHistoryDto dtoTo = new IngredientHistoryDto();
            dtoTo.setIngredient(ingredient);
            dtoTo.setWarehouse(to);
            dtoTo.setTimestamp(c.getDate());
            dtoTo.setQuantityChange(c.getQuantity());
            dtoTo.setNotes(toNote);
            dtoTo.setAction(action);
            ingredientHistoryService.recordQuantityChange(dtoTo);

        }
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<TransferResponse>, PaginationMetadata> getPagination(
            Map<String, FilterCriteria> filters,
            Integer page,
            Integer size,
            String sortBy,
            String sortDir,
            ComponentType type
    ) {
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);

        Specification<Transfer> spec = Specification
                .where(TransferSpecification.hasType(type))
                .and(TransferSpecification.buildSpecification(filters, type));

        Page<Transfer> transferPage = transferRepository.findAll(spec, pageable);

        List<TransferResponse> content = mapTransfersToResponses(transferPage.getContent(), type);
        PaginationMetadata metadata = buildPaginationMetadata(transferPage, filters, pageable);

        return Pair.of(content, metadata);
    }

    private List<TransferResponse> mapTransfersToResponses(List<Transfer> transfers, ComponentType type) {
        List<TransferResponse> responses = new ArrayList<>();
        for (Transfer t : transfers) {
            TransferResponse r = new TransferResponse();
            r.setDate(t.getDate());
            r.setFromWarehouseName(t.getFrom() != null ? t.getFrom().getName() : null);
            r.setToWarehouseName(t.getTo() != null ? t.getTo().getName() : null);
            r.setQuantity(t.getQuantity());
            r.setComponentName(fetchComponentName(t.getComponentId(), type));
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

    private PaginationMetadata buildPaginationMetadata(Page<Transfer> page, Map<String, FilterCriteria> filters, Pageable pageable) {
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
                "transferTable"
        );
    }
}
