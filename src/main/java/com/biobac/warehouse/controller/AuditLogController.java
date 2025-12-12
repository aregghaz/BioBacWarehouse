package com.biobac.warehouse.controller;

import com.biobac.warehouse.entity.AuditLog;
import com.biobac.warehouse.response.ApiResponse;
import com.biobac.warehouse.service.AuditLogService;
import com.biobac.warehouse.utils.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/warehouse/logs")
@RequiredArgsConstructor
public class AuditLogController {
    private final AuditLogService auditLogService;

    @GetMapping("/audit")
    public ApiResponse<List<AuditLog>> getAuditLogs() {
        List<AuditLog> auditLogs = auditLogService.getAuditLogs();
        return ResponseUtil.success("Audit logs retrieved successfully", auditLogs);
    }
}
