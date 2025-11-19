package com.biobac.warehouse.repository;

import com.biobac.warehouse.entity.ComponentBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ComponentBalanceRepository extends JpaRepository<ComponentBalance, Long>, JpaSpecificationExecutor<ComponentBalance> {
    Optional<ComponentBalance> findByWarehouseIdAndIngredientId(Long warehouseId, Long ingredientId);
    Optional<ComponentBalance> findByWarehouseIdAndProductId(Long warehouseId, Long productId);
}
