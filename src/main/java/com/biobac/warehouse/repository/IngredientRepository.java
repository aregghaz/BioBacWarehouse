package com.biobac.warehouse.repository;

import com.biobac.warehouse.entity.Ingredient;
import com.biobac.warehouse.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface IngredientRepository extends JpaRepository<Ingredient, Long>, JpaSpecificationExecutor<Ingredient> {
    Optional<Ingredient> findByIdAndDeletedFalse(Long id);
    List<Ingredient> findAllByDeletedFalse();
}
