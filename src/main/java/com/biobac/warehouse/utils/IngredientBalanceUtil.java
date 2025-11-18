package com.biobac.warehouse.utils;

import com.biobac.warehouse.entity.Ingredient;
import com.biobac.warehouse.entity.IngredientBalance;
import com.biobac.warehouse.entity.Warehouse;
import com.biobac.warehouse.exception.InvalidDataException;
import com.biobac.warehouse.repository.IngredientBalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IngredientBalanceUtil {

  private final IngredientBalanceRepository ingredientBalanceRepository;

  public IngredientBalance getOrCreateIngredientBalance(Warehouse warehouse, Ingredient ingredient) {
    if (warehouse == null || warehouse.getId() == null) {
      throw new InvalidDataException("Warehouse is required for component balance (ingredient)");
    }
    if (ingredient == null || ingredient.getId() == null) {
      throw new InvalidDataException("Ingredient is required for component balance");
    }
    return ingredientBalanceRepository.findByWarehouseAndIngredient(warehouse, ingredient)
        .orElseGet(() -> {
          IngredientBalance cb = new IngredientBalance();
          cb.setWarehouse(warehouse);
          cb.setIngredient(ingredient);
          cb.setBalance(0.0);
          return ingredientBalanceRepository.save(cb);
        });
  }

}
