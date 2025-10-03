package com.biobac.warehouse.mapper;

import com.biobac.warehouse.entity.ComponentBalance;
import com.biobac.warehouse.entity.InventoryItem;
import com.biobac.warehouse.repository.InventoryItemRepository;
import com.biobac.warehouse.response.ComponentBalanceIngResponse;
import com.biobac.warehouse.response.ComponentBalanceProdResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Component
public class ComponentBalanceMapper {

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    public ComponentBalanceIngResponse toIngResponse(ComponentBalance entity) {
        if(entity.getIngredient() == null){
            return null;
        }
        ComponentBalanceIngResponse response = new ComponentBalanceIngResponse();
        response.setName(entity.getIngredient().getName());
        response.setBalance(entity.getBalance());
        response.setIngredientGroupName(entity.getIngredient().getIngredientGroup().getName());
        response.setExpirationDate(getLastExpirationDate(entity));
        return response;
    }

    public ComponentBalanceProdResponse toProdResponse(ComponentBalance entity) {
        if(entity.getProduct() == null){
            return null;
        }
        ComponentBalanceProdResponse response = new ComponentBalanceProdResponse();
        response.setName(entity.getIngredient().getName());
        response.setBalance(entity.getBalance());
        response.setProductGroupName(entity.getProduct().getProductGroup().getName());
        response.setExpirationDate(getLastExpirationDate(entity));
        return response;
    }

    private LocalDate getLastExpirationDate(ComponentBalance componentBalance) {
        List<InventoryItem> list;

        if (componentBalance.getProduct() != null) {
            list = inventoryItemRepository.findByProductId(componentBalance.getProduct().getId());
        } else {
            list = inventoryItemRepository.findByIngredientId(componentBalance.getIngredient().getId());
        }

        LocalDate today = LocalDate.now();

        return list.stream()
                .map(InventoryItem::getExpirationDate)
                .filter(Objects::nonNull)
                .min(Comparator.comparing(d ->
                        Math.abs(ChronoUnit.DAYS.between(today, d))
                ))
                .orElse(null);
    }
}
