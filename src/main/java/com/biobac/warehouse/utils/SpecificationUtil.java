package com.biobac.warehouse.utils;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SpecificationUtil {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(DateUtil.TIME_FORMAT);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DateUtil.DATE_FORMAT);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DateUtil.DATE_TIME_FORMAT);

    public static Predicate buildEquals(CriteriaBuilder cb, Path<?> path, Object value) {
        if (Number.class.isAssignableFrom(path.getJavaType())) {
            return cb.equal(path.as(Number.class), Double.parseDouble(value.toString()));
        } else if (Boolean.class.isAssignableFrom(path.getJavaType()) || boolean.class.isAssignableFrom(path.getJavaType())) {
            Boolean bool = Boolean.parseBoolean(value.toString());
            return cb.equal(path.as(Boolean.class), bool);
        } else if (LocalDate.class.isAssignableFrom(path.getJavaType())) {
            LocalDate date = LocalDate.parse(value.toString(), DATE_FORMATTER);
            return cb.equal(path.as(LocalDate.class), date);
        } else if (LocalTime.class.isAssignableFrom(path.getJavaType())) {
            LocalTime time = LocalTime.parse(value.toString(), TIME_FORMATTER);
            return cb.equal(path.as(LocalTime.class), time);
        } else if (LocalDateTime.class.isAssignableFrom(path.getJavaType())) {
            LocalDateTime dateTime = LocalDateTime.parse(value.toString(), DATE_TIME_FORMATTER);
            return cb.equal(path.as(LocalDateTime.class), dateTime);
        } else {
            return cb.equal(cb.lower(path.as(String.class)), value.toString().toLowerCase().trim());
        }
    }

    public static Predicate buildNotEquals(CriteriaBuilder cb, Path<?> path, Object value) {
        if (Number.class.isAssignableFrom(path.getJavaType())) {
            return cb.notEqual(path.as(Number.class), Double.parseDouble(value.toString()));
        } else if (Boolean.class.isAssignableFrom(path.getJavaType()) || boolean.class.isAssignableFrom(path.getJavaType())) {
            Boolean bool = Boolean.parseBoolean(value.toString());
            return cb.notEqual(path.as(Boolean.class), bool);
        } else if (LocalDate.class.isAssignableFrom(path.getJavaType())) {
            LocalDate date = LocalDate.parse(value.toString(), DATE_FORMATTER);
            return cb.notEqual(path.as(LocalDate.class), date);
        } else if (LocalTime.class.isAssignableFrom(path.getJavaType())) {
            LocalTime time = LocalTime.parse(value.toString(), TIME_FORMATTER);
            return cb.notEqual(path.as(LocalTime.class), time);
        } else if (LocalDateTime.class.isAssignableFrom(path.getJavaType())) {
            LocalDateTime dateTime = LocalDateTime.parse(value.toString(), DATE_TIME_FORMATTER);
            return cb.notEqual(path.as(LocalDateTime.class), dateTime);
        } else {
            return cb.notEqual(cb.lower(path.as(String.class)), value.toString().toLowerCase().trim());
        }
    }

    public static Predicate buildGreaterThanOrEqualTo(CriteriaBuilder cb, Path<?> path, Object value) {
        if (Number.class.isAssignableFrom(path.getJavaType())) {
            return cb.greaterThanOrEqualTo(path.as(Double.class), Double.parseDouble(value.toString()));
        } else if (LocalDate.class.isAssignableFrom(path.getJavaType())) {
            LocalDate date = LocalDate.parse(value.toString(), DATE_FORMATTER);
            return cb.greaterThanOrEqualTo(path.as(LocalDate.class), date);
        } else if (LocalTime.class.isAssignableFrom(path.getJavaType())) {
            LocalTime time = LocalTime.parse(value.toString(), TIME_FORMATTER);
            return cb.greaterThanOrEqualTo(path.as(LocalTime.class), time);
        } else if (LocalDateTime.class.isAssignableFrom(path.getJavaType())) {
            LocalDateTime dateTime = LocalDateTime.parse(value.toString(), DATE_TIME_FORMATTER);
            return cb.greaterThanOrEqualTo(path.as(LocalDateTime.class), dateTime);
        }
        return null;
    }

    public static Predicate buildLessThanOrEqualTo(CriteriaBuilder cb, Path<?> path, Object value) {
        if (Number.class.isAssignableFrom(path.getJavaType())) {
            return cb.lessThanOrEqualTo(path.as(Double.class), Double.parseDouble(value.toString()));
        } else if (LocalDate.class.isAssignableFrom(path.getJavaType())) {
            LocalDate date = LocalDate.parse(value.toString(), DATE_FORMATTER);
            return cb.lessThanOrEqualTo(path.as(LocalDate.class), date);
        } else if (LocalTime.class.isAssignableFrom(path.getJavaType())) {
            LocalTime time = LocalTime.parse(value.toString(), TIME_FORMATTER);
            return cb.lessThanOrEqualTo(path.as(LocalTime.class), time);
        } else if (LocalDateTime.class.isAssignableFrom(path.getJavaType())) {
            LocalDateTime dateTime = LocalDateTime.parse(value.toString(), DATE_TIME_FORMATTER);
            return cb.lessThanOrEqualTo(path.as(LocalDateTime.class), dateTime);
        }
        return null;
    }

    public static Predicate buildBetween(CriteriaBuilder cb, Path<?> path, Object value) {
        if (!(value instanceof List<?> values) || values.size() != 2) return null;
        Object start = values.get(0);
        Object end = values.get(1);

        if (Number.class.isAssignableFrom(path.getJavaType())) {
            return cb.between(path.as(Double.class),
                    Double.parseDouble(start.toString()),
                    Double.parseDouble(end.toString()));
        } else if (LocalDate.class.isAssignableFrom(path.getJavaType())) {
            LocalDate startDate = LocalDate.parse(start.toString(), DATE_FORMATTER);
            LocalDate endDate = LocalDate.parse(end.toString(), DATE_FORMATTER);
            return cb.between(path.as(LocalDate.class), startDate, endDate);
        } else if (LocalTime.class.isAssignableFrom(path.getJavaType())) {
            LocalTime startTime = LocalTime.parse(start.toString(), TIME_FORMATTER);
            LocalTime endTime = LocalTime.parse(end.toString(), TIME_FORMATTER);
            return cb.between(path.as(LocalTime.class), startTime, endTime);
        } else if (LocalDateTime.class.isAssignableFrom(path.getJavaType())) {
            LocalDateTime startDateTime = LocalDateTime.parse(start.toString(), DATE_TIME_FORMATTER);
            LocalDateTime endDateTime = LocalDateTime.parse(end.toString(), DATE_TIME_FORMATTER);
            return cb.between(path.as(LocalDateTime.class), startDateTime, endDateTime);
        }
        return null;
    }
}
