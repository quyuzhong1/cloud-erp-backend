package com.common.core.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson2.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Will
 * @version 1.0
 * @description: 对象Map互转
 * @date 2022/12/1 19:52
 */
public class BeanMapUtil {


    /**
     * 对象转Map（支持多重嵌套对象转换Map）
     * @param object
     * @return Map
     */
    public static Map objToMap(Object object)  {
        String bean = JSONObject.toJSONString(object);
        //bean  to Map
        Map<String, JSONObject> tempMap = JSON.parseObject(bean, new HashMap<String, JSONObject>().getClass());
        return tempMap;
    }

    /**
     * 反射对象转Map（多重嵌套对象未转换Map）
     * @param object
     * @return
     * @throws IllegalAccessException
     */
    public static Map beanToMap(Object object) throws IllegalAccessException {
        Map<String, Object> map = new HashMap<String, Object>();
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            map.put(field.getName(), field.get(object));
        }
        return map;
    }

    /**
     * map转对象
     * @param map
     * @param beanClass
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> T mapToBean(Map map, Class<T> beanClass) throws Exception {
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



}
