package com.biobac.warehouse.utils;

import com.biobac.warehouse.request.FilterCriteria;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class DateUtil {
    public static final String DATE_TIME_FORMAT = "dd/MM/yyyy:HH:mm:ss";
    public static final String DATE_FORMAT = "dd/MM/yyyy";
    public static final String TIME_FORMAT = "HH:mm:ss";

    public static LocalDate parseDate(String date) {
        return LocalDate.parse(date, DateTimeFormatter.ofPattern(DATE_FORMAT));
    }

    public static LocalDateTime parseDateTime(String dateTime) {
        return LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
    }

    public static LocalDate parseDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.toLocalDate();
    }

    public static List<LocalDateTime> parseDates(Map<String, FilterCriteria> filters) {
        LocalDateTime startDate = null;
        LocalDateTime endDate = null;
        if (filters != null) {
            FilterCriteria ts = filters.get("timestamp");
            if (ts != null && ts.getOperator() != null) {
                String op = ts.getOperator();
                Object val = ts.getValue();
                try {
                    if ("between".equals(op) && val instanceof List<?> list && list.size() == 2) {
                        startDate = DateUtil.parseDateTime(String.valueOf(list.get(0)));
                        endDate = DateUtil.parseDateTime(String.valueOf(list.get(1)));
                    }
                } catch (Exception ignore) {
                }
            }
        }
        return List.of(startDate, endDate);
    }
}
