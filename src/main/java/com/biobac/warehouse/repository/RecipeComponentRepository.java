package com.biobac.warehouse.repository;

import com.biobac.warehouse.entity.RecipeComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeComponentRepository extends JpaRepository<RecipeComponent, Long>, JpaSpecificationExecutor<RecipeComponent> {
}
