package com.cloud.erp.gateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Feign配置类
 * 解决Spring Cloud Gateway中缺少HttpMessageConverters Bean的问题
 * 同时解决LocalDateTime格式解析问题和ApiResult字段不匹配问题
 *
 * @author wuhaotian
 * @since 2025-01-27
 */
@Configuration
public class FeignConfig {

    /**
     * 配置HttpMessageConverters Bean
     * 解决feign.codec.DecodeException: No qualifying bean of type 'org.springframework.boot.autoconfigure.http.HttpMessageConverters' available
     * 同时解决LocalDateTime格式解析问题和ApiResult字段不匹配问题
     */
    @Bean
    public HttpMessageConverters httpMessageConverters() {
        List<HttpMessageConverter<?>> converters = new ArrayList<>();
        
        // 创建ObjectMapper并配置
        ObjectMapper objectMapper = new ObjectMapper();
        
        // 禁用将日期时间写为时间戳
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // 忽略未知字段，解决ApiResult中success字段不匹配问题
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        // 配置JavaTimeModule来处理LocalDateTime
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        
        // 定义多种日期时间格式
        DateTimeFormatter[] formatters = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"),
            DateTimeFormatter.ISO_LOCAL_DATE_TIME
        };
        
        // 创建自定义的LocalDateTime反序列化器，支持多种格式
        LocalDateTimeDeserializer deserializer = new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) {
            @Override
            public LocalDateTime deserialize(com.fasterxml.jackson.core.JsonParser p, com.fasterxml.jackson.databind.DeserializationContext ctxt) throws java.io.IOException {
                String dateTimeString = p.getValueAsString();
                if (dateTimeString == null || dateTimeString.trim().isEmpty()) {
                    return null;
                }
                
                // 尝试多种格式解析
                for (DateTimeFormatter formatter : formatters) {
                    try {
                        return LocalDateTime.parse(dateTimeString, formatter);
                    } catch (DateTimeParseException e) {
                        // 继续尝试下一个格式
                    }
                }
                
                // 如果所有格式都失败，抛出异常
                throw new IllegalArgumentException("无法解析日期时间字符串: " + dateTimeString);
            }
        };
        
        // 配置LocalDateTime的序列化和反序列化
        javaTimeModule.addDeserializer(LocalDateTime.class, deserializer);
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        objectMapper.registerModule(javaTimeModule);
        
        // 创建Jackson消息转换器
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        jsonConverter.setObjectMapper(objectMapper);
        converters.add(jsonConverter);
        
        return new HttpMessageConverters(converters);
    }
}
