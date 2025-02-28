package com.common.core.utils;

import com.github.dozermapper.core.DozerBeanMapperBuilder;
import com.github.dozermapper.core.Mapper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
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

    public static <S, T> T copyNonNull(S source, T target) throws IllegalAccessException {
        if (source == null || target == null) {
            throw new IllegalArgumentException("Source and target objects must not be null");
        }

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
                        targetField.set(target, value);
                    }
                }
            }
        }

        return target;
    }

}
