package com.biobac.warehouse.controller;

import com.biobac.warehouse.entity.HistoryAction;
import com.biobac.warehouse.response.ApiResponse;
import com.biobac.warehouse.service.InfoService;
import com.biobac.warehouse.utils.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/warehouse/info")
@RequiredArgsConstructor
public class InfoController {
    private final InfoService infoService;

    @GetMapping("/history-action")
    public ApiResponse<List<HistoryAction>> getActions() {
        List<HistoryAction> actions = infoService.getActions();
        return ResponseUtil.success("Actions fetched successfully", actions);
    }
}
