package com.biobac.warehouse.repository;

import com.biobac.warehouse.entity.IngredientGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface IngredientGroupRepository extends JpaRepository<IngredientGroup, Long>, JpaSpecificationExecutor<IngredientGroup> {

}
