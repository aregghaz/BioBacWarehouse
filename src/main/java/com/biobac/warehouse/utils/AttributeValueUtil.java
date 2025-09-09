package com.biobac.warehouse.utils;

import com.biobac.warehouse.entity.AttributeDataType;
import com.biobac.warehouse.exception.InvalidDataException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class AttributeValueUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DateUtil.DATE_FORMAT);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(DateUtil.TIME_FORMAT);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DateUtil.DATE_TIME_FORMAT);

    private AttributeValueUtil() {
    }

    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static boolean isNumber(String value) {
        if (isBlank(value)) return true;
        try {
            new BigDecimal(value.trim());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isDate(String value) {
        if (isBlank(value)) return true;
        try {
            LocalDate.parse(value.trim(), DATE_FORMATTER);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isTime(String value) {
        if (isBlank(value)) return true;
        try {
            LocalTime.parse(value.trim(), TIME_FORMATTER);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isDateTime(String value) {
        if (isBlank(value)) return true;
        try {
            LocalDateTime.parse(value.trim(), DATE_TIME_FORMATTER);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static List<String> parseMultiSelect(String value) {
        if (isBlank(value)) return Collections.emptyList();
        String[] parts = value.split(",");
        List<String> res = new ArrayList<>();
        for (String p : parts) {
            String t = p.trim();
            if (!t.isEmpty()) {
                res.add(t);
            }
        }
        return res;
    }

    public static boolean isValid(AttributeDataType type, String value) {
        if (type == null) return false;
        switch (type) {
            case NUMBER:
                return isNumber(value);
            case DATE:
                return isDate(value);
            case TIME:
                return isTime(value);
            case DATE_TIME:
                return isDateTime(value);
//            case MULTISELECT:
//                // Any list of comma-separated tokens is acceptable; blank tokens are ignored
//                return true;
//            case SELECT:
            case INPUT:
            case TEXTAREA:
                // free text; always valid (blank considered valid)
                return true;
            default:
                return true;
        }
    }

    public static void validateOrThrow(AttributeDataType type, String value) {
        Objects.requireNonNull(type, "Attribute data type is required");
        if (isBlank(value)) return;
        switch (type) {
            case NUMBER:
                if (!isNumber(value)) {
                    throw new InvalidDataException("Invalid NUMBER format: '" + value + "'");
                }
                break;
            case DATE:
                if (!isDate(value)) {
                    throw new InvalidDataException("Invalid DATE format (expected " + DateUtil.DATE_FORMAT + "): '" + value + "'");
                }
                break;
            case TIME:
                if (!isTime(value)) {
                    throw new InvalidDataException("Invalid TIME format (expected " + DateUtil.TIME_FORMAT + "): '" + value + "'");
                }
                break;
            case DATE_TIME:
                if (!isDateTime(value)) {
                    throw new InvalidDataException("Invalid DATE_TIME format (expected " + DateUtil.DATE_TIME_FORMAT + "): '" + value + "'");
                }
                break;
//            case MULTISELECT:
//            case SELECT:
            case INPUT:
            case TEXTAREA:
                break;
            default:
                break;
        }
    }

    public static Object parse(AttributeDataType type, String value) {
        if (isBlank(value)) return null;
        switch (type) {
            case NUMBER:
                return new BigDecimal(value.trim());
            case DATE:
                return LocalDate.parse(value.trim(), DATE_FORMATTER);
            case TIME:
                return LocalTime.parse(value.trim(), TIME_FORMATTER);
            case DATE_TIME:
                return LocalDateTime.parse(value.trim(), DATE_TIME_FORMATTER);
//            case MULTISELECT:
//                return parseMultiSelect(value);
//            case SELECT:
            case INPUT:
            case TEXTAREA:
            default:
                return value;
        }
    }
}