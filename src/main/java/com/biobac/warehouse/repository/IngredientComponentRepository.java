package com.biobac.warehouse.repository;

import com.biobac.warehouse.entity.IngredientComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IngredientComponentRepository extends JpaRepository<IngredientComponent, Long> {
    List<IngredientComponent> findByParentIngredientId(Long parentId);
    List<IngredientComponent> findByChildIngredientId(Long childId);
}