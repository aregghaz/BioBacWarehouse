package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.dto.IngredientHistoryDto;
import com.biobac.warehouse.entity.Ingredient;
import com.biobac.warehouse.entity.IngredientHistory;
import com.biobac.warehouse.mapper.IngredientHistoryMapper;
import com.biobac.warehouse.repository.IngredientHistoryRepository;
import com.biobac.warehouse.service.IngredientHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IngredientHistoryServiceImpl implements IngredientHistoryService {

    private final IngredientHistoryRepository ingredientHistoryRepository;
    private final IngredientHistoryMapper ingredientHistoryMapper;

    @Override
    @Transactional
    public IngredientHistoryDto recordQuantityChange(Ingredient ingredient, Double quantityBefore, 
                                                    Double quantityAfter, String action, String notes) {
        IngredientHistory history = new IngredientHistory();
        history.setIngredient(ingredient);
        history.setTimestamp(LocalDateTime.now());
        history.setAction(action);
        history.setQuantityBefore(quantityBefore);
        history.setQuantityAfter(quantityAfter);
        history.setNotes(notes);
        
        IngredientHistory savedHistory = ingredientHistoryRepository.save(history);
        return ingredientHistoryMapper.toDto(savedHistory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IngredientHistoryDto> getHistoryForIngredient(Long ingredientId) {
        List<IngredientHistory> historyList = ingredientHistoryRepository.findByIngredientIdOrderByTimestampDesc(ingredientId);
        return ingredientHistoryMapper.toDtoList(historyList);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IngredientHistoryDto> getHistoryForDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<IngredientHistory> historyList = ingredientHistoryRepository.findByTimestampBetweenOrderByTimestampDesc(startDate, endDate);
        return ingredientHistoryMapper.toDtoList(historyList);
    }
}