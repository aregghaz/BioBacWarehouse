package com.biobac.warehouse.config;

import com.biobac.warehouse.utils.DateUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DateUtil.DATE_TIME_FORMAT);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DateUtil.DATE_FORMAT);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(DateUtil.TIME_FORMAT);

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addFormatterForFieldType(LocalDateTime.class, new org.springframework.format.Formatter<LocalDateTime>() {
            @Override
            public String print(LocalDateTime object, java.util.Locale locale) {
                return object.format(DATE_TIME_FORMATTER);
            }

            @Override
            public LocalDateTime parse(String text, java.util.Locale locale) {
                return LocalDateTime.parse(text, DATE_TIME_FORMATTER);
            }
        });

        registry.addFormatterForFieldType(LocalDate.class, new org.springframework.format.Formatter<LocalDate>() {
            @Override
            public String print(LocalDate object, java.util.Locale locale) {
                return object.format(DATE_FORMATTER);
            }

            @Override
            public LocalDate parse(String text, java.util.Locale locale) {
                return LocalDate.parse(text, DATE_FORMATTER);
            }
        });

        registry.addFormatterForFieldType(LocalTime.class, new org.springframework.format.Formatter<LocalTime>() {
            @Override
            public String print(LocalTime object, java.util.Locale locale) {
                return object.format(TIME_FORMATTER);
            }

            @Override
            public LocalTime parse(String text, java.util.Locale locale) {
                return LocalTime.parse(text, TIME_FORMATTER);
            }
        });
    }
}