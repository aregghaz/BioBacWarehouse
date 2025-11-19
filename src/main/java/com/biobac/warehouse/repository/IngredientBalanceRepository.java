package com.biobac.warehouse.repository;

import com.biobac.warehouse.entity.Ingredient;
import com.biobac.warehouse.entity.IngredientBalance;
import com.biobac.warehouse.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface IngredientBalanceRepository extends JpaRepository<IngredientBalance, Long>, JpaSpecificationExecutor<IngredientBalance> {
    Optional<IngredientBalance> findByWarehouseAndIngredient(Warehouse warehouse, Ingredient ingredient);

    @Query("select coalesce(sum(b.balance), 0) from IngredientBalance b where b.ingredient.id = :ingredientId")
    Double sumBalanceByIngredientId(@Param("ingredientId") Long ingredientId);
}
