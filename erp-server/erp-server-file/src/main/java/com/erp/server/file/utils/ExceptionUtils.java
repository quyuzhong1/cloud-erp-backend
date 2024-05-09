package com.erp.server.file.utils;


import com.erp.server.file.exception.BusinessException;

import java.util.Objects;
import java.util.function.BooleanSupplier;

public final class ExceptionUtils {
    private ExceptionUtils(){}

    public static void emptyThrow(Object o, String message) {
        if (Objects.isNull(o)){
            throw new BusinessException(message);
        }
    }

    public static void conditionThrow(BooleanSupplier condition, String message) {
        if (condition.getAsBoolean()) {
            throw new BusinessException(message);
        }
    }
}