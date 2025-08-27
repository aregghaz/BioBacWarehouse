package com.biobac.warehouse.repository;

import com.biobac.warehouse.entity.Ingredient;
import com.biobac.warehouse.entity.Product;
import com.biobac.warehouse.entity.RecipeItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeItemRepository extends JpaRepository<RecipeItem, Long>, JpaSpecificationExecutor<RecipeItem> {
}