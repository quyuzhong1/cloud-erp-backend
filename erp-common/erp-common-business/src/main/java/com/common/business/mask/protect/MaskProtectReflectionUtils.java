package com.common.business.mask.protect;

import com.common.business.utils.ConvertUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * 脱敏回显保护反射工具。
 *
 * @author cloud-erp
 */
final class MaskProtectReflectionUtils {

    private MaskProtectReflectionUtils() {
    }

    static Field findField(Class<?> clazz, String fieldName) {
        if (clazz == null || fieldName == null || fieldName.isEmpty()) {
            return null;
        }
        Field[] fields = ConvertUtils.getAllFields(clazz);
        if (fields == null) {
            return null;
        }
        for (Field field : fields) {
            if (fieldName.equals(field.getName())) {
                int mod = field.getModifiers();
                if (Modifier.isStatic(mod) || Modifier.isFinal(mod) || Modifier.isTransient(mod)) {
                    return null;
                }
                field.setAccessible(true);
                return field;
            }
        }
        return null;
    }

    static Object getFieldValue(Object owner, String fieldName) {
        if (owner == null) {
            return null;
        }
        Field field = findField(owner.getClass(), fieldName);
        if (field == null) {
            return null;
        }
        try {
            return field.get(owner);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    static String normalizeValue(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof LocalDateTime) {
            return String.valueOf(((LocalDateTime) value).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        }
        if (value instanceof LocalDate) {
            return String.valueOf(((LocalDate) value).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());
        }
        if (value instanceof Date) {
            return String.valueOf(((Date) value).getTime());
        }
        return String.valueOf(value);
    }

    static Object convertString(String value, Class<?> targetType) {
        if (targetType == null || targetType == String.class || targetType == Object.class) {
            return value;
        }
        if (value == null) {
            return null;
        }
        if (targetType == BigDecimal.class) {
            return new BigDecimal(value);
        }
        if (targetType == BigInteger.class) {
            return new BigInteger(value);
        }
        if (targetType == Integer.class || targetType == int.class) {
            return Integer.valueOf(value);
        }
        if (targetType == Long.class || targetType == long.class) {
            return Long.valueOf(value);
        }
        if (targetType == Double.class || targetType == double.class) {
            return Double.valueOf(value);
        }
        if (targetType == Float.class || targetType == float.class) {
            return Float.valueOf(value);
        }
        if (targetType == Short.class || targetType == short.class) {
            return Short.valueOf(value);
        }
        if (targetType == Byte.class || targetType == byte.class) {
            return Byte.valueOf(value);
        }
        if (targetType == Boolean.class || targetType == boolean.class) {
            return Boolean.valueOf(value);
        }
        if (targetType == Character.class || targetType == char.class) {
            return value.isEmpty() ? null : value.charAt(0);
        }
        if (targetType == LocalDateTime.class) {
            return parseLocalDateTime(value);
        }
        if (targetType == LocalDate.class) {
            return parseLocalDate(value);
        }
        if (targetType == LocalTime.class) {
            return LocalTime.parse(value);
        }
        if (Date.class.isAssignableFrom(targetType)) {
            return new Date(Long.parseLong(value));
        }
        return value;
    }

    private static LocalDateTime parseLocalDateTime(String value) {
        if (value.matches("^-?\\d+$")) {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(value)), ZoneId.systemDefault());
        }
        return LocalDateTime.parse(value);
    }

    private static LocalDate parseLocalDate(String value) {
        if (value.matches("^-?\\d+$")) {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(value)), ZoneId.systemDefault())
                    .toLocalDate();
        }
        return LocalDate.parse(value);
    }
}
