package com.common.core.enums;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ApiError registry for enum-compatible values()/valueOf().
 * <p>
 * 从各分片类反射收集 {@code public static final ApiError}，避免手维护 values() 数组漏登。
 * 注意：不要反射 {@link ApiError} 门面类，其中多为分片常量的别名，会重复注册。
 */
final class ApiErrorRegistry {

    /** 分片顺序保持稳定，便于 values() 顺序可预期。 */
    private static final Class<?>[] SHARD_CLASSES = {
            ApiErrorCommon.class,
            ApiErrorSys.class,
            ApiErrorOms.class,
            ApiErrorWms.class,
            ApiErrorTms.class,
            ApiErrorPlm.class,
            ApiErrorScm.class,
            ApiErrorSrm.class,
            ApiErrorDmp.class,
            ApiErrorFms.class,
            ApiErrorWorkflow.class,
    };

    private static final ApiError[] VALUES;
    private static final Map<String, ApiError> BY_NAME;

    static {
        List<ApiError> values = new ArrayList<>();
        Map<String, ApiError> byName = new HashMap<>();
        for (Class<?> shardClass : SHARD_CLASSES) {
            collect(shardClass, values, byName);
        }
        VALUES = values.toArray(new ApiError[0]);
        BY_NAME = byName;
    }

    private ApiErrorRegistry() {
    }

    private static void collect(Class<?> shardClass, List<ApiError> values, Map<String, ApiError> byName) {
        for (Field field : shardClass.getDeclaredFields()) {
            int modifiers = field.getModifiers();
            if (!Modifier.isPublic(modifiers) || !Modifier.isStatic(modifiers) || !Modifier.isFinal(modifiers)) {
                continue;
            }
            if (field.getType() != ApiError.class) {
                continue;
            }
            final ApiError error;
            try {
                error = (ApiError) field.get(null);
            } catch (IllegalAccessException e) {
                throw new ExceptionInInitializerError(
                        "Failed to read ApiError constant: " + shardClass.getSimpleName() + "." + field.getName());
            }
            if (error == null) {
                throw new ExceptionInInitializerError(
                        "ApiError constant is null: " + shardClass.getSimpleName() + "." + field.getName());
            }
            ApiError previous = byName.put(error.name(), error);
            if (previous != null) {
                throw new ExceptionInInitializerError(
                        "Duplicate ApiError name: " + error.name()
                                + " at " + shardClass.getSimpleName() + "." + field.getName());
            }
            values.add(error);
        }
    }

    static ApiError[] values() {
        return VALUES.clone();
    }

    static ApiError valueOf(String name) {
        if (name == null) {
            throw new NullPointerException("Name is null");
        }
        ApiError value = BY_NAME.get(name);
        if (value == null) {
            throw new IllegalArgumentException("No enum constant com.common.core.enums.ApiError." + name);
        }
        return value;
    }
}
