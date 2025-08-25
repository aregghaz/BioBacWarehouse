package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.entity.AuditLog;
import com.biobac.warehouse.repository.AuditLogRepository;
import com.biobac.warehouse.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {
    private final AuditLogRepository repo;

    @Override
    public void logCreate(String entityName, Long entityId, Object newObject, String username) {
        for (Field field : newObject.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object newValue = field.get(newObject);
                if (newValue != null) {
                    if (isEntity(newValue)) {
                        parseNestedObject(newValue, entityName, entityId, username, "CREATE");
                    } else {
                        saveLog(entityName, entityId, field.getName(), null, newValue.toString(), username, "CREATE");
                    }
                }
            } catch (IllegalAccessException e) {
                System.out.println("Error accessing field: " + field.getName());
            }
        }
    }

    @Override
    public void logUpdate(String entityName, Long entityId, Object oldObject, Object newObject, String username) {
        for (Field field : newObject.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object oldValue = field.get(oldObject);
                Object newValue = field.get(newObject);

                if (!Objects.equals(oldValue, newValue)) {
                    if (newValue != null && isEntity(newValue)) {
                        parseNestedObject(newValue, entityName, entityId, username, "UPDATE");
                    } else {
                        saveLog(entityName, entityId, field.getName(),
                                oldValue != null ? oldValue.toString() : null,
                                newValue != null ? newValue.toString() : null,
                                username, "UPDATE");
                    }
                }
            } catch (IllegalAccessException e) {
                System.out.println("Error accessing field: " + field.getName());
            }
        }
    }

    @Override
    public void logDelete(String entityName, Long entityId, String username) {
        saveLog(entityName, entityId, null, null, null, username, "DELETE");
    }

    @Override
    public List<AuditLog> getAuditLogs() {
        return repo.findAll();
    }

    private void saveLog(String entityName, Long entityId, String field, String oldValue, String newValue, String username, String action) {
        AuditLog log = new AuditLog();
        log.setEntityName(entityName);
        log.setEntityId(entityId);
        log.setField(field);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setUser(username);
        log.setTimestamp(LocalDateTime.now());
        log.setAction(action);

        repo.save(log);
    }

    private void parseNestedObject(Object object, String entityName, Long entityId, String username, String action) {
        for (Field field : object.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(object);
                if (value != null) {
                    if (isEntity(value)) {
                        parseNestedObject(value, entityName, entityId, username, action);
                    } else {
                        saveLog(entityName, entityId, field.getName(), null, value.toString(), username, action);
                    }
                }
            } catch (IllegalAccessException e) {
                System.out.println("Error accessing field: " + field.getName());
            }
        }
    }

    private boolean isEntity(Object obj) {
        String packageName = obj.getClass().getPackageName();
        return packageName.startsWith("com.biobac.warehouse.entity") ||
                packageName.startsWith("com.biobac.warehouse.dto");
    }
}
