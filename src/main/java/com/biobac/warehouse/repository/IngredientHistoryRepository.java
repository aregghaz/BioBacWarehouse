package com.biobac.warehouse.repository;

import com.biobac.warehouse.entity.IngredientHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface IngredientHistoryRepository extends JpaRepository<IngredientHistory, Long>, JpaSpecificationExecutor<IngredientHistory> {
}