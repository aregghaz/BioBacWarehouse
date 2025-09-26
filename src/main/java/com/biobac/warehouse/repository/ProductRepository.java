package com.biobac.warehouse.repository;

import com.biobac.warehouse.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    Optional<Product> findByIdAndDeletedFalse(Long id);

    List<Product> findAllByDeletedFalse();

    Product findBySku(String sku);

    @Query("""
            SELECT p
            FROM Product p
            WHERE p.deleted = false
              AND p.id NOT IN (
                  SELECT rc.ingredient.id
                  FROM RecipeComponent rc
                  WHERE rc.recipeItem.id = :recipeItemId
                    AND rc.ingredient IS NOT NULL
              )
            """)
    List<Product> findAllByDeletedFalseExcludeRecipe(@Param("recipeItemId") Long recipeItemId);
}
