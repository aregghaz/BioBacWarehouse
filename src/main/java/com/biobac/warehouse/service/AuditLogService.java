package com.biobac.warehouse.service;

import com.biobac.warehouse.entity.AuditLog;

import java.util.List;

public interface AuditLogService {
    void logCreate(String entityName, Long entityId, Object newObject, String username);

    void logUpdate(String entityName, Long entityId, Object oldObject, Object newObject, String username);

    void logDelete(String entityName, Long entityId, String username);

    List<AuditLog> getAuditLogs();
}
