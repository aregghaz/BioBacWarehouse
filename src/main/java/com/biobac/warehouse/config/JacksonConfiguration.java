package com.biobac.warehouse.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.datatype.jsr310.deser.*;
import com.fasterxml.jackson.datatype.jsr310.ser.*;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
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

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer customBigDecimalFormatter() {
        return builder -> {
            builder.serializerByType(BigDecimal.class, new JsonSerializer<BigDecimal>() {
                @Override
                public void serialize(BigDecimal value, JsonGenerator gen, SerializerProvider serializers)
                        throws IOException {
                    if (value == null) {
                        gen.writeNull();
                        return;
                    }
                    gen.writeString(value.setScale(2, RoundingMode.HALF_UP).toPlainString());
                }
            });
        };
    }
}
