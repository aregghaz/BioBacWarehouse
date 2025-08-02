package com.biobac.warehouse.repository;

import com.biobac.warehouse.entity.RecipeItem;
import com.biobac.warehouse.entity.Product;
import com.biobac.warehouse.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeItemRepository extends JpaRepository<RecipeItem, Long> {
    List<RecipeItem> findByProduct(Product product);
    List<RecipeItem> findByIngredient(Ingredient ingredient);
    List<RecipeItem> findByProductId(Long productId);
    List<RecipeItem> findByIngredientId(Long ingredientId);
}