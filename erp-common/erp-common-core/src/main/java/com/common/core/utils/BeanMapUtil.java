package com.common.core.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * @author Will
 * @version 1.0
 * @description: 对象Map互转
 * @date 2022/12/1 19:52
 */
@Slf4j
public class BeanMapUtil {

    private BeanMapUtil() {
    }

    // 为保证可见性和有序性，防止出现半初始化
    private static volatile BeanMapUtil INSTANCE;

    /**
     * 获取单例
     * @return
     */
    public static BeanMapUtil getInstance() {
        if (INSTANCE == null) {
            synchronized (BeanMapUtil.class) {
                if (null == INSTANCE) {
                    INSTANCE = new BeanMapUtil();
                    return INSTANCE;
                }
            }
        }
        return INSTANCE;
    }


    /**
     * 对象转Map（支持多重嵌套对象转换Map）
     * @param object
     * @return Map
     */
    public static Map<String, Object> objToMap(Object object)  {
        JSON bean = (JSON)JSON.toJSON(object);
        //bean  to Map
        return JSON.toJavaObject(bean, Map.class);
    }

    /**
     * 反射对象转Map（多重嵌套对象未转换Map）
     * @param object
     * @return
     * @throws IllegalAccessException
     */
    public static Map<String, Object> beanToMap(Object object) throws IllegalAccessException {
        Map<String, Object> map = new HashMap<>();
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            map.put(field.getName(), field.get(object));
        }
        return map;
    }

    public static <T> List<Map<String, Object>> beanToMapList(List<T> objects) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object object : objects) {
            Map<String, Object> map = JSON.parseObject(JSON.toJSONString(object), new TypeReference<Map<String, Object>>(){});
            result.add(map);
        }
        return result;
    }

    /**
     * map转对象
     * @param map
     * @param beanClass
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> T mapToBean(Map<String, Object> map, Class<T> beanClass) throws InstantiationException, IllegalAccessException {
        T object = beanClass.newInstance();
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            int mod = field.getModifiers();
            if (Modifier.isStatic(mod) || Modifier.isFinal(mod)) {
                continue;
            }
            field.setAccessible(true);
            if (map.containsKey(field.getName())) {
                field.set(object, map.get(field.getName()));
            }
        }
        return object;
    }

    /**
     * 取Map集合的差集
     */
    public static <S,T> Map<S, T> getDifferenceSetByGuava(Map<S, T> leftMap, Map<S, T> rightMap) {
        if (null != leftMap && null != rightMap) {

            Set<S> leftMapKey = leftMap.keySet();
            Set<S> rightMapKey = rightMap.keySet();
            Set<S> differenceSet = Sets.difference(leftMapKey, rightMapKey);
            Map<S, T> result = Maps.newHashMap();
            for (S key : differenceSet) {
                result.put(key, leftMap.get(key));
            }
            return result;

        } else {
            return Collections.emptyMap();
        }
    }

    /**
     * 取Map集合的并集
     */
    public static <S,T> Map<S, T> getUnionSetByGuava(Map<S, T> leftMap, Map<S, T> rightMap) {
        if (null != leftMap && null != rightMap) {

            Set<S> leftMapKey = leftMap.keySet();
            Set<S> rightMapKey = rightMap.keySet();
            Set<S> differenceSet = Sets.union(leftMapKey, rightMapKey);
            Map<S, T> result = Maps.newHashMap();
            for (S key : differenceSet) {
                if (leftMap.containsKey(key)) {
                    result.put(key, leftMap.get(key));
                } else {
                    result.put(key, rightMap.get(key));
                }
            }
            return result;

        } else {
            return Collections.emptyMap();
        }
    }

    /**
     * 驼峰转换
     * @param source
     * @param target
     * @param <K>
     * @param <T>
     * @return
     */
    public <K, T> T copyAndParse(K source, T target) {
        // 下划线转驼峰
        BeanUtil.copyProperties(source, target, getCopyOptions(source.getClass()));
        return target;
    }

    // 缓存CopyOptions（注意这个是HuTool的类，不是Cglib的）

    private Map<Class<?>, CopyOptions> cacheMap = new HashMap<>();


    private CopyOptions getCopyOptions(Class<?> source) {
        CopyOptions options = cacheMap.get(source);
        if (options == null) {
            // 不加锁，我们认为重复执行不会比并发加锁带来的开销大
            options = CopyOptions.create().setFieldMapping(buildFieldMapper(source));
            cacheMap.put(source, options);
        }
        return options;
    }

    /**
     * @param source
     * @return
     */
    private Map<String, String> buildFieldMapper(Class<?> source) {
        PropertyDescriptor[] properties = org.springframework.cglib.core.ReflectUtils.getBeanProperties(source);
        Map<String, String> map = new HashMap<>();
        for (PropertyDescriptor target : properties) {
            String name = target.getName();
            String camel = CharSequenceUtil.toCamelCase(name);
            if (!name.equalsIgnoreCase(camel)) {
                map.put(name, camel);
            }
            String under = CharSequenceUtil.toUnderlineCase(name);
            if (!name.equalsIgnoreCase(under)) {
                map.put(name, under);
            }
        }
        return map;
    }

}
