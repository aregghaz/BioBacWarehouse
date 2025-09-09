package com.biobac.warehouse.repository;

import com.biobac.warehouse.entity.AttributeDefinition;
import com.biobac.warehouse.entity.AttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttributeValueRepository extends JpaRepository<AttributeValue, Long> {
    Optional<AttributeValue> findByDefinition_IdAndIngredient_Id(Long definitionId, Long ingredientId);

    List<AttributeValue> findByIngredient_IdAndDeletedFalse(Long ingredientId);

    Optional<AttributeValue> findByDefinition_IdAndProduct_Id(Long definitionId, Long productId);

    List<AttributeValue> findByProduct_IdAndDeletedFalse(Long productId);

    Optional<AttributeValue> findByDefinition_IdAndWarehouse_Id(Long definitionId, Long warehouseId);

    List<AttributeValue> findByWarehouse_IdAndDeletedFalse(Long warehouseId);

    void deleteByIngredient_Id(Long ingredientId);

    void deleteByProduct_Id(Long productId);

    void deleteByWarehouse_Id(Long warehouseId);

    AttributeValue findByDefinition(AttributeDefinition definition);
}
