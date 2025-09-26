package com.biobac.warehouse.repository;

import com.biobac.warehouse.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IngredientRepository extends JpaRepository<Ingredient, Long>, JpaSpecificationExecutor<Ingredient> {
    Optional<Ingredient> findByIdAndDeletedFalse(Long id);

    List<Ingredient> findAllByDeletedFalse();

//    @Query("""
//            SELECT i
//            FROM Ingredient i
//            WHERE i.deleted = false
//              AND i.id NOT IN (
//                  SELECT ri.ingredient.id
//                  FROM RecipeItem ri
//                  WHERE ri.ingredient IS NOT NULL
//              )
//              AND i.id NOT IN (
//                  SELECT rc.ingredient.id
//                  FROM RecipeComponent rc
//                  WHERE rc.recipeItem.id = :recipeItemId
//                    AND rc.ingredient IS NOT NULL
//              )
//            """)
//    List<Ingredient> findAllByDeletedFalseExcludeRecipe(@Param("recipeItemId") Long recipeItemId);
}
