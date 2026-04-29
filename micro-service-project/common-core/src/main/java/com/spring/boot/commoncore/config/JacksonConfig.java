package com.spring.boot.commoncore.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/4/27 14:50
 */
@Configuration
public class JacksonConfig {

	@Bean
	public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

		JavaTimeModule module = new JavaTimeModule();
		module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(formatter));
		module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(formatter));

		return builder.modules(module).build();
	}
}
