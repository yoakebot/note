import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

@Configuration
public class DateTimeSerializerConfig {

    private static final List<String> DATE_PATTERNS = Arrays.asList(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd",
            "H:m:s"
    );

    // 自定义反序列化器
    public static class CustomLocalDateTimeDeserializer extends LocalDateTimeDeserializer {
        public CustomLocalDateTimeDeserializer() {
            super(DateTimeFormatter.ofPattern(DATE_PATTERNS.get(0)));
        }

        @Override
        public LocalDateTime deserialize(JsonParser parser, DeserializationContext context)
                throws IOException {
            String dateString = parser.getText().trim();

            if(dateString.isEmpty()) {
                return null;
            }

            // 尝试所有支持的格式
            for (String pattern : DATE_PATTERNS) {
                try {
                    switch (pattern) {
                        case "yyyy-MM-dd" -> {
                            return LocalDateTime.of(
                                    LocalDate.parse(dateString, DateTimeFormatter.ofPattern(pattern)),
                                    LocalTime.MIDNIGHT
                            );
                        }
                        case "H:m:s" -> {
                            LocalTime time = LocalTime.parse(dateString, DateTimeFormatter.ofPattern("H:m:s"));
                            return LocalDateTime.of(LocalDate.now(), time);
                        }
                        case "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" -> {
                            return Instant.parse(dateString)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDateTime();
                        }
                    }
                    return LocalDateTime.parse(dateString,
                            DateTimeFormatter.ofPattern(pattern));
                } catch (DateTimeParseException ignored) {
                }
            }

            // 如果所有格式都解析失败，抛出异常
            throw new DateTimeParseException(
                    "Cannot parse date time: " + dateString, dateString, 0);
        }
    }

    @Bean
    public LocalDateTimeSerializer localDateTimeSerializer() {
        return new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATE_PATTERNS.get(1)));
    }

    @Bean
    public LocalDateTimeDeserializer localDateTimeDeserializer() {
        return new CustomLocalDateTimeDeserializer();
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonObjectMapperCustomization() {
        return jacksonObjectMapperBuilder -> {
            jacksonObjectMapperBuilder.timeZone(TimeZone.getTimeZone("GMT+8"))
                    .serializerByType(LocalDateTime.class, localDateTimeSerializer())
                    .deserializerByType(LocalDateTime.class, localDateTimeDeserializer())
                    .failOnEmptyBeans(false)
                    .featuresToEnable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
                    .featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                            SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                    .dateFormat(new SimpleDateFormat(DATE_PATTERNS.get(1)));
        };
    }
}
