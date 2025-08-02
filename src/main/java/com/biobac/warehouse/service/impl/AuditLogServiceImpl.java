package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.entity.AuditLog;
import com.biobac.warehouse.repository.AuditLogRepository;
import com.biobac.warehouse.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {
    private final AuditLogRepository repo;

    public void log(String entityName, Long entityId, String field, String oldVal, String newVal, String user) {
        AuditLog log = new AuditLog();
        log.setEntityName(entityName);
        log.setEntityId(entityId);
        log.setField(field);
        log.setOldValue(oldVal);
        log.setNewValue(newVal);
        log.setUser(user);
        log.setTimestamp(LocalDateTime.now());
        repo.save(log);
    }
}
