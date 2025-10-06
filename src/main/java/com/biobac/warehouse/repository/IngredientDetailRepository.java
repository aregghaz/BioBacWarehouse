package com.biobac.warehouse.repository;

import com.biobac.warehouse.entity.IngredientDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface IngredientDetailRepository extends JpaRepository<IngredientDetail, Long>, JpaSpecificationExecutor<IngredientDetail> {
    List<IngredientDetail> findByIngredientBalanceIdOrderByExpirationDateAsc(Long ingredientBalanceId);
}
