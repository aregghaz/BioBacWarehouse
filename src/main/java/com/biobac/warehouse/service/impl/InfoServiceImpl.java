package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.entity.HistoryAction;
import com.biobac.warehouse.repository.HistoryActionRepository;
import com.biobac.warehouse.service.InfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InfoServiceImpl implements InfoService {
    private final HistoryActionRepository historyActionRepository;

    @Override
    public List<HistoryAction> getActions() {
        return historyActionRepository.findAll().stream().toList();
    }
}
