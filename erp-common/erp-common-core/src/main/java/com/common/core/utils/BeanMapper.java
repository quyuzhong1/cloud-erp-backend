package com.common.core.utils;

import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.github.dozermapper.core.DozerBeanMapperBuilder;
import com.github.dozermapper.core.Mapper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Classname BeanMapper

 * @Date 2022-09-15 16:01
 * @Created by yl
 */
public class BeanMapper {
    private BeanMapper() {
    }

    private static Mapper dozerBeanMapper =  DozerBeanMapperBuilder.buildDefault();

    public static void copy(Object source, Object destinationObject) {
        if (source != null) {
            dozerBeanMapper.map(source, destinationObject);
        }
    }


    public static <T> List<T> copyList(Iterable<?> sourceList, Class<T> destinationClass) {
        List<T> destinationList = new ArrayList<>();
        for (Object sourceObject : sourceList) {
            T destinationObject = dozerBeanMapper.map(sourceObject, destinationClass);
            destinationList.add(destinationObject);
        }
        return destinationList;
    }

    public static <S, T> T copyNonNull(S source, T target)  {
        if (source == null || target == null) {
            throw new IllegalArgumentException("Source and target objects must not be null");
        }
        try {
            Field[] sourceFields = source.getClass().getDeclaredFields();
            Field[] targetFields = target.getClass().getDeclaredFields();

            Map<String, Field> targetFieldMap = new HashMap<>();
            for (Field field : targetFields) {
                field.setAccessible(true);
                targetFieldMap.put(field.getName(), field);
            }

            for (Field sourceField : sourceFields) {
                sourceField.setAccessible(true);
                Object value = sourceField.get(source);
                if (value != null) {
                    Field targetField = targetFieldMap.get(sourceField.getName());
                    if (targetField != null) {
                        int mod = targetField.getModifiers();
                        if (!Modifier.isStatic(mod) && !Modifier.isFinal(mod)) {
                            targetField.setAccessible(true);
                            try {
                                // 尝试进行类型转换
                                Object convertedValue = convertValue(value, targetField.getType());
                                targetField.set(target, convertedValue);
                            } catch (Exception e) {
                                // 如果转换失败，可以选择跳过该字段或记录日志
                                System.out.println("Failed to convert field " + sourceField.getName() + ": " + e.getMessage());
                            }
                        }
                    }
                }
            }
            return target;
        } catch (IllegalAccessException e) {
            throw new ServiceException(ApiError.COMMON_COPY_FAILED,e.getMessage());
        }
    }

    private static Object convertValue(Object value, Class<?> targetType) {
        if (targetType.isInstance(value)) {
            return value;
        }
        if (targetType == Integer.class || targetType == int.class) {
            return Integer.parseInt(value.toString());
        } else if (targetType == Long.class || targetType == long.class) {
            return Long.parseLong(value.toString());
        } else if (targetType == Double.class || targetType == double.class) {
            return Double.parseDouble(value.toString());
        } else if (targetType == Float.class || targetType == float.class) {
            return Float.parseFloat(value.toString());
        } else if (targetType == Boolean.class || targetType == boolean.class) {
            return Boolean.parseBoolean(value.toString());
        } else if (targetType == LocalDate.class) {
            return parseToLocalDate(value.toString());
        }else if (targetType == LocalDateTime.class) {
            return parseToLocalDateTime(value.toString());
        }
        // 可以继续添加其他类型的转换逻辑
        throw new IllegalArgumentException("Unsupported type conversion: " + value.getClass() + " to " + targetType);
    }

    private static LocalDate parseToLocalDate(String dateStr) {
        List<DateTimeFormatter> formatters = new ArrayList<>();
        formatters.add(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        formatters.add(DateTimeFormatter.ofPattern("yyyy-M-d"));
        formatters.add(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(dateStr, formatter);
            } catch (DateTimeParseException e) {
                // 继续尝试下一个格式
            }
        }
        throw new IllegalArgumentException("Failed to parse LocalDate from string: " + dateStr);
    }
    private static LocalDate parseToLocalDateTime(String dateStr) {
        List<DateTimeFormatter> formatters = new ArrayList<>();
        formatters.add(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
        formatters.add(DateTimeFormatter.ofPattern("yyyy-M-d HH:mm:ss"));
        formatters.add(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(dateStr, formatter);
            } catch (DateTimeParseException e) {
                // 继续尝试下一个格式
            }
        }
        throw new IllegalArgumentException("Failed to parse LocalDate from string: " + dateStr);
    }

}
