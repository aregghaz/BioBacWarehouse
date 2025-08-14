package com.biobac.warehouse.config;

import com.fasterxml.jackson.datatype.jsr310.deser.*;
import com.fasterxml.jackson.datatype.jsr310.ser.*;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.format.DateTimeFormatter;

import static com.biobac.warehouse.utils.DateUtil.*;

@Configuration
public class JacksonConfiguration {
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
        DateTimeFormatter localDateFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        DateTimeFormatter localTimeFormatter = DateTimeFormatter.ofPattern(TIME_FORMAT);
        return builder -> {
            builder.serializers(new LocalDateTimeSerializer(dateTimeFormatter));
            builder.serializers(new LocalDateSerializer(localDateFormatter));
            builder.serializers(new LocalTimeSerializer(localTimeFormatter));
            builder.deserializers(new LocalDateTimeDeserializer(dateTimeFormatter));
            builder.deserializers(new LocalDateDeserializer(localDateFormatter));
            builder.deserializers(new LocalTimeDeserializer(localTimeFormatter));
        };
    }
}
