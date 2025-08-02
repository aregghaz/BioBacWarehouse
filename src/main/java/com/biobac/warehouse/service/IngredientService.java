
package com.biobac.warehouse.service;
import com.biobac.warehouse.dto.IngredientDto;
import com.biobac.warehouse.entity.Ingredient;
import com.biobac.warehouse.entity.IngredientGroup;
import com.biobac.warehouse.mapper.IngredientMapper;
import com.biobac.warehouse.repository.IngredientGroupRepository;
import com.biobac.warehouse.repository.IngredientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


public interface IngredientService {


    List<IngredientDto> getAll();


    IngredientDto getById(Long id);


    IngredientDto create(IngredientDto dto);


    IngredientDto update(Long id, IngredientDto dto);


    void delete(Long id);
}
