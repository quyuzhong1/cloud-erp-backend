package com.sdk.third.tf.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import lombok.extern.slf4j.Slf4j;

/**
 * JSON工具类
 * 使用Jackson进行序列化和反序列化，支持：
 * 1. 识别 @JsonProperty 注解（序列化时使用注解指定的字段名）
 * 2. 兼容下划线和驼峰命名（反序列化时）
 * 
 * @author system
 * @date 2025/01/XX
 */
@Slf4j
public class JsonUtil {

    /**
     * ObjectMapper实例（单例）- 用于序列化
     * 配置：
     * - 识别 @JsonProperty 注解，使用注解指定的字段名
     * - 不序列化null值
     */
    private static final ObjectMapper SERIALIZE_MAPPER = createSerializeMapper();
    
    /**
     * ObjectMapper实例（单例）- 用于反序列化
     * 配置：
     * - 识别 @JsonProperty 注解
     * - 兼容下划线和驼峰命名
     * - 忽略未知属性
     */
    private static final ObjectMapper DESERIALIZE_MAPPER = createDeserializeMapper();

    /**
     * 创建并配置用于序列化的ObjectMapper
     */
    private static ObjectMapper createSerializeMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // 忽略无法转换的对象（序列化时）
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        
        // 不序列化null值
        mapper.setSerializationInclusion(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL);
        
        // 启用注解支持（识别@JsonProperty）
        mapper.configure(MapperFeature.USE_ANNOTATIONS, true);
        
        // 使用自定义命名策略：优先使用@JsonProperty注解指定的名称
        mapper.setPropertyNamingStrategy(new CompatibleNamingStrategy());
        
        return mapper;
    }
    
    /**
     * 创建并配置用于反序列化的ObjectMapper
     * 兼容下划线和驼峰命名
     */
    private static ObjectMapper createDeserializeMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // 忽略JSON字符串中不识别的属性（反序列化时）
        // 这样即使字段名不完全匹配也不会报错
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        // 启用注解支持（识别@JsonProperty）
        mapper.configure(MapperFeature.USE_ANNOTATIONS, true);
        
        // 配置大小写不敏感（反序列化时）
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        
        // 使用自定义命名策略：优先使用@JsonProperty注解指定的名称
        // 同时兼容下划线和驼峰命名
        mapper.setPropertyNamingStrategy(new CompatibleNamingStrategy());
        
        return mapper;
    }

    /**
     * 兼容下划线和驼峰命名的自定义命名策略
     * 优先使用@JsonProperty注解指定的名称
     * 如果没有注解，保持原样（不转换）
     */
    private static class CompatibleNamingStrategy extends PropertyNamingStrategy {
        @Override
        public String nameForField(MapperConfig<?> config, AnnotatedField field, String defaultName) {
            // 如果有@JsonProperty注解，使用注解指定的名称
            com.fasterxml.jackson.annotation.JsonProperty jsonProperty = 
                field.getAnnotation(com.fasterxml.jackson.annotation.JsonProperty.class);
            if (jsonProperty != null && !jsonProperty.value().isEmpty()) {
                return jsonProperty.value();
            }
            // 否则保持原样
            return defaultName;
        }

        @Override
        public String nameForGetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
            // 如果有@JsonProperty注解，使用注解指定的名称
            com.fasterxml.jackson.annotation.JsonProperty jsonProperty = 
                method.getAnnotation(com.fasterxml.jackson.annotation.JsonProperty.class);
            if (jsonProperty != null && !jsonProperty.value().isEmpty()) {
                return jsonProperty.value();
            }
            // 否则保持原样
            return defaultName;
        }

        @Override
        public String nameForSetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
            // 如果有@JsonProperty注解，使用注解指定的名称
            com.fasterxml.jackson.annotation.JsonProperty jsonProperty = 
                method.getAnnotation(com.fasterxml.jackson.annotation.JsonProperty.class);
            if (jsonProperty != null && !jsonProperty.value().isEmpty()) {
                return jsonProperty.value();
            }
            // 否则保持原样
            return defaultName;
        }
    }

    /**
     * 将对象序列化为JSON字符串
     * 会识别 @JsonProperty 注解，使用注解指定的字段名
     * 
     * @param obj 要序列化的对象
     * @return JSON字符串
     */
    public static String toJsonString(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return SERIALIZE_MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("对象序列化为JSON失败", e);
            throw new RuntimeException("对象序列化为JSON失败: " + e.getMessage(), e);
        }
    }

    /**
     * 将JSON字符串反序列化为对象
     * 兼容下划线和驼峰命名
     * 
     * @param json JSON字符串
     * @param clazz 目标类型
     * @return 反序列化后的对象
     */
    public static <T> T parseObject(String json, Class<T> clazz) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            // 使用配置好的反序列化Mapper（会识别@JsonProperty，兼容下划线和驼峰）
            return DESERIALIZE_MAPPER.readValue(json, clazz);
        } catch (Exception e) {
            log.error("JSON反序列化失败, json: {}, class: {}", json, clazz.getName(), e);
            throw new RuntimeException("JSON反序列化失败: " + e.getMessage(), e);
        }
    }

    /**
     * 将JSON字符串反序列化为对象（支持泛型）
     * 兼容下划线和驼峰命名
     * 
     * @param json JSON字符串
     * @param typeReference 类型引用
     * @return 反序列化后的对象
     */
    public static <T> T parseObject(String json, TypeReference<T> typeReference) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            // 使用配置好的反序列化Mapper（会识别@JsonProperty，兼容下划线和驼峰）
            return DESERIALIZE_MAPPER.readValue(json, typeReference);
        } catch (Exception e) {
            log.error("JSON反序列化失败, json: {}, typeReference: {}", json, typeReference.getType(), e);
            throw new RuntimeException("JSON反序列化失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取用于序列化的ObjectMapper实例（用于高级用法）
     * 
     * @return ObjectMapper实例
     */
    public static ObjectMapper getSerializeMapper() {
        return SERIALIZE_MAPPER;
    }
    
    /**
     * 获取用于反序列化的ObjectMapper实例（用于高级用法）
     * 
     * @return ObjectMapper实例
     */
    public static ObjectMapper getDeserializeMapper() {
        return DESERIALIZE_MAPPER;
    }
}
