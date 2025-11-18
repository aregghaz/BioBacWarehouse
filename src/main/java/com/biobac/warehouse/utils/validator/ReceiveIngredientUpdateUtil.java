package com.biobac.warehouse.utils.validator;

import com.biobac.warehouse.entity.Ingredient;
import com.biobac.warehouse.entity.ReceiveIngredient;
import com.biobac.warehouse.entity.ReceiveIngredientStatus;
import com.biobac.warehouse.exception.NotFoundException;
import com.biobac.warehouse.repository.ReceiveIngredientStatusRepository;
import com.biobac.warehouse.request.ReceiveIngredientUpdateRequest;

import java.time.LocalDateTime;

public final class ReceiveIngredientUpdateUtil {

    private ReceiveIngredientUpdateUtil() {}

    public static void applyConditionalUpdates(
            ReceiveIngredient item,
            ReceiveIngredientUpdateRequest updateRequest,
            Ingredient ingredient,
            boolean canUpdateStatus,
            ReceiveIngredientStatusRepository statusRepository) {

        if (updateRequest.getImportDate() != null) {
            item.setImportDate(updateRequest.getImportDate());
        }

        if (updateRequest.getManufacturingDate() != null) {
            item.setManufacturingDate(updateRequest.getManufacturingDate());
        }

        // Calculate expiration date if manufacturing date and ingredient expiration are available
        updateExpirationDate(item, ingredient);

        if (updateRequest.getCompanyId() != null) {
            item.setCompanyId(updateRequest.getCompanyId());
        }

        if (canUpdateStatus && updateRequest.getStatusId() != null) {
            ReceiveIngredientStatus status = statusRepository.findById(updateRequest.getStatusId())
                    .orElseThrow(() -> new NotFoundException("Status not found"));
            item.setStatus(status);
        }
    }

    /**
     * Updates expiration date based on manufacturing date and ingredient expiration period.
     *
     * @param item the ReceiveIngredient entity
     * @param ingredient the ingredient containing expiration period
     */
    private static void updateExpirationDate(ReceiveIngredient item, Ingredient ingredient) {
        LocalDateTime manufacturingDate = item.getManufacturingDate();
        if (manufacturingDate != null && ingredient != null && ingredient.getExpiration() != null) {
            item.setExpirationDate(manufacturingDate.plusDays(ingredient.getExpiration()));
        }
    }

}
