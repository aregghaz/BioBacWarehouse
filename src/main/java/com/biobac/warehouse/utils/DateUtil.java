package com.biobac.warehouse.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateUtil {
    public static final String DATE_TIME_FORMAT = "dd/MM/yyyy:HH:mm:ss";
    public static final String DATE_FORMAT = "dd/MM/yyyy";
    public static final String TIME_FORMAT = "HH:mm:ss";

    public static LocalDate parseDate(String date) {
        return LocalDate.parse(date, DateTimeFormatter.ofPattern(DATE_FORMAT));
    }
}
