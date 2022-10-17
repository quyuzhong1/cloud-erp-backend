package com.common.core.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Classname ObjectUtils
 * @Description TODO
 * @Date 2022-10-15 16:24
 * @Created by yl
 */
public class ObjectUtils {

    public static Map<String, Field> getFieldList(Object obj) {
        Map<String, Field> fieldMap = new LinkedHashMap<>();
        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (needFilterField(field)) {
                continue;
            }
            fieldMap.put(field.getName(), field);
        }
        getParentField(clazz, fieldMap);
        return fieldMap;
    }


    /**
     * 过滤不需要属性
     *
     * @param field
     * @return
     */
    private static Boolean needFilterField(Field field) {
        // 过滤静态属性
        if (Modifier.isStatic(field.getModifiers())) {
            return true;
        }
        // 过滤transient 关键字修饰的属性
        if (Modifier.isTransient(field.getModifiers())) {
            return true;
        }
        return false;
    }

    /**
     * 递归所有父类属性
     *
     * @param clazz
     * @param fieldMap
     */
    private static void getParentField(Class<?> clazz, Map<String, Field> fieldMap) {
        Class<?> superClazz = clazz.getSuperclass();
        if (superClazz != null) {
            Field[] superFields = superClazz.getDeclaredFields();
            for (Field field : superFields) {
                if (needFilterField(field)) {
                    continue;
                }
                fieldMap.put(field.getName(), field);
            }
            getParentField(superClazz, fieldMap);
        }
    }

    /**
     * 根据属性名获取属性值
     *
     * @param obj
     * @param fieldName
     * @return
     */
    public static Object getFieldValue(Object obj, String fieldName) {
        try {
            Map<String, Method> methodMap = getMethodMap(obj);
            String firstLetter = fieldName.substring(0, 1).toUpperCase();
            String getter = "get" + firstLetter + fieldName.substring(1);
            Method method = methodMap.get(getter);
            Object value = null;
            if (method != null) {
                value = method.invoke(obj, new Object[]{});
            }
            return value;
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * 根据类获取属性信息和父类的属性信息
     *
     * @param obj
     * @return
     */
    public static Map<String, Method> getMethodMap(Object obj) {
        Map<String, Method> methodMap = new LinkedHashMap<>();
        Class<?> clazz = obj.getClass();
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            methodMap.put(method.getName(), method);
        }
        getParentMethod(clazz, methodMap);
        return methodMap;
    }

    /**
     * 递归所有父类方法
     *
     * @param clazz
     * @param methodMap
     */
    private static void getParentMethod(Class<?> clazz, Map<String, Method> methodMap) {
        Class<?> superClazz = clazz.getSuperclass();
        if (superClazz != null) {
            Method[] superMethods = superClazz.getMethods();
            for (Method field : superMethods) {
                methodMap.put(field.getName(), field);
            }
            getParentMethod(superClazz, methodMap);
        }
    }

    /**
     * 设置属性值
     *
     * @param obj
     * @param fieldName
     * @param value
     */
    public static void setFieldValue(Object obj, String fieldName, Object value) {
        try {
            Map<String, Field> fields = getFieldList(obj);
            Field f = fields.get(fieldName);
            if (f != null) {
                f.setAccessible(true);
                f.set(obj, value);
            }
        } catch (Exception e) {
        }
    }
}
