package com.biobac.warehouse.repository;

import com.biobac.warehouse.entity.AttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttributeValueRepository extends JpaRepository<AttributeValue, Long> {
    Optional<AttributeValue> findByDefinition_IdAndIngredient_Id(Long definitionId, Long ingredientId);
    List<AttributeValue> findByIngredient_IdAndDeletedFalse(Long ingredientId);
}
