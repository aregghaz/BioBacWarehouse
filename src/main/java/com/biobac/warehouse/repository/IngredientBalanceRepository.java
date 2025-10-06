package com.biobac.warehouse.repository;

import com.biobac.warehouse.entity.IngredientBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface IngredientBalanceRepository extends JpaRepository<IngredientBalance, Long>, JpaSpecificationExecutor<IngredientBalance> {
    Optional<IngredientBalance> findByWarehouseIdAndIngredientId(Long id, Long id1);
}
