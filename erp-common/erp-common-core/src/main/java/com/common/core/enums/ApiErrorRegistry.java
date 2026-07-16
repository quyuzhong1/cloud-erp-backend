package com.common.core.enums;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ApiError registry for enum-compatible values()/valueOf().
 */
final class ApiErrorRegistry {

    private static final ApiError[] VALUES;
    private static final Map<String, ApiError> BY_NAME;

    static {
        List<ApiError> values = new ArrayList<>();
        add(values, ApiErrorCommon.values());
        add(values, ApiErrorSys.values());
        add(values, ApiErrorOms.values());
        add(values, ApiErrorWms.values());
        add(values, ApiErrorTms.values());
        add(values, ApiErrorPlm.values());
        add(values, ApiErrorScm.values());
        add(values, ApiErrorSrm.values());
        add(values, ApiErrorDmp.values());
        add(values, ApiErrorFms.values());
        add(values, ApiErrorWorkflow.values());
        VALUES = values.toArray(new ApiError[0]);
        BY_NAME = new HashMap<>(VALUES.length);
        for (ApiError value : VALUES) {
            BY_NAME.put(value.name(), value);
        }
    }

    private ApiErrorRegistry() {
    }

    private static void add(List<ApiError> values, ApiError[] items) {
        for (ApiError item : items) {
            values.add(item);
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
