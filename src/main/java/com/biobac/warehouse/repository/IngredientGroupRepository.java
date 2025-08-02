package com.biobac.warehouse.repository;
import com.biobac.warehouse.entity.IngredientGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngredientGroupRepository extends JpaRepository<IngredientGroup, Long> {
}
