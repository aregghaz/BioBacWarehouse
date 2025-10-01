package com.biobac.warehouse.repository;

import com.biobac.warehouse.entity.ComponentBalance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ComponentBalanceRepository extends JpaRepository<ComponentBalance, Long> {
    Optional<ComponentBalance> findByWarehouseIdAndIngredientId(Long warehouseId, Long ingredientId);
    Optional<ComponentBalance> findByWarehouseIdAndProductId(Long warehouseId, Long productId);
}
