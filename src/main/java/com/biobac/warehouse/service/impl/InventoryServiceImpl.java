package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.dto.ChangeComponentDto;
import com.biobac.warehouse.dto.TransferComponentDto;
import com.biobac.warehouse.entity.*;
import com.biobac.warehouse.exception.NotFoundException;
import com.biobac.warehouse.repository.*;
import com.biobac.warehouse.response.ComponentBalanceQuantityResponse;
import com.biobac.warehouse.service.IngredientHistoryService;
import com.biobac.warehouse.service.InventoryService;
import com.biobac.warehouse.service.ProductHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

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

    @Override
    @Transactional
    public void transfer(List<TransferComponentDto> componentDtoList) {
        if (componentDtoList.isEmpty()) {
            return;
        }

        componentDtoList.forEach(c -> {
            if (Objects.equals(c.getComponentType(), "ingredient")) {
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
                if (requiredQty <= 0) return;

                fromBalance.setBalance(fromBalance.getBalance() - requiredQty);
                toBalance.setBalance(toBalance.getBalance() + requiredQty);
                ingredientBalanceRepository.saveAll(List.of(fromBalance, toBalance));

                List<IngredientDetail> fromDetails = ingredientDetailRepository
                        .findByIngredientBalanceIdOrderByExpirationDateAsc(fromBalance.getId());

                double remaining = requiredQty;

                for (IngredientDetail fromDetail : fromDetails) {
                    if (remaining <= 0) break;

                    double available = fromDetail.getQuantity();
                    double toTransfer = Math.min(available, remaining);

                    fromDetail.setQuantity(available - toTransfer);
                    ingredientDetailRepository.save(fromDetail);

                    IngredientDetail toDetail = ingredientDetailRepository
                            .findByIngredientBalanceIdAndExpirationDate(toBalance.getId(), fromDetail.getExpirationDate())
                            .orElseGet(() -> {
                                IngredientDetail newDetail = new IngredientDetail();
                                newDetail.setIngredientBalance(toBalance);
                                newDetail.setExpirationDate(fromDetail.getExpirationDate());
                                newDetail.setQuantity(0.0);
                                return newDetail;
                            });

                    toDetail.setQuantity(toDetail.getQuantity() + toTransfer);
                    ingredientDetailRepository.save(toDetail);

                    remaining -= toTransfer;
                }
            } else if (Objects.equals(c.getComponentType(), "product")) {
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
                if (requiredQty <= 0) return;

                fromBalance.setBalance(fromBalance.getBalance() - requiredQty);
                toBalance.setBalance(toBalance.getBalance() + requiredQty);
                productBalanceRepository.saveAll(List.of(fromBalance, toBalance));

                List<ProductDetail> fromDetails = productDetailRepository
                        .findByProductBalanceIdOrderByExpirationDateAsc(fromBalance.getId());

                double remaining = requiredQty;

                for (ProductDetail fromDetail : fromDetails) {
                    if (remaining <= 0) break;

                    double available = fromDetail.getQuantity();
                    double toTransfer = Math.min(available, remaining);

                    fromDetail.setQuantity(available - toTransfer);
                    productDetailRepository.save(fromDetail);

                    ProductDetail toDetail = productDetailRepository
                            .findByProductBalanceIdAndExpirationDate(toBalance.getId(), fromDetail.getExpirationDate())
                            .orElseGet(() -> {
                                ProductDetail newDetail = new ProductDetail();
                                newDetail.setProductBalance(toBalance);
                                newDetail.setExpirationDate(fromDetail.getExpirationDate());
                                newDetail.setQuantity(0.0);
                                return newDetail;
                            });

                    toDetail.setQuantity(toDetail.getQuantity() + toTransfer);
                    productDetailRepository.save(toDetail);

                    remaining -= toTransfer;
                }
            }
        });
    }

    @Override
    @Transactional
    public void change(List<ChangeComponentDto> componentDtoList) {

    }

    @Override
    @Transactional(readOnly = true)
    public ComponentBalanceQuantityResponse getIngredientBalance(Long ingredientId, Long warehouseId) {
        Ingredient ingredient = ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> new NotFoundException("Ingredient not found"));
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new NotFoundException("Warehouse not found"));
        IngredientBalance ingredientBalance = ingredientBalanceRepository.findByWarehouseAndIngredient(warehouse, ingredient)
                .orElse(null);

        Double balance = ingredientBalance != null ? ingredientBalance.getBalance() : 0.0;
        ComponentBalanceQuantityResponse response = new ComponentBalanceQuantityResponse();
        response.setBalance(balance);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public ComponentBalanceQuantityResponse getProductBalance(Long productId, Long warehouseId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Ingredient not found"));
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new NotFoundException("Warehouse not found"));
        ProductBalance productBalance = productBalanceRepository.findByWarehouseAndProduct(warehouse, product)
                .orElse(null);

        Double balance = productBalance != null ? productBalance.getBalance() : 0.0;
        ComponentBalanceQuantityResponse response = new ComponentBalanceQuantityResponse();
        response.setBalance(balance);
        return response;
    }
}
