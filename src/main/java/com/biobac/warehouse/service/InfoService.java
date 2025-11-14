package com.biobac.warehouse.service;

import com.biobac.warehouse.entity.HistoryAction;

import java.util.List;

public interface InfoService {
    List<HistoryAction> getActions();
}
