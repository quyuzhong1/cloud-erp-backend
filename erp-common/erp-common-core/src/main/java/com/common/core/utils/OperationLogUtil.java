package com.common.core.utils;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.core.utils.date.DateUtil;
import com.common.core.utils.date.EnumTimePattern;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import javassist.bytecode.stackmap.TypeData;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.math3.util.Pair;
import org.apache.poi.ss.formula.functions.T;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Will
 * @version 1.0
 * @description: 操作日志util
 * @date 2022/12/1 21:14
 */
public class OperationLogUtil {



    public static Map<Pair<String,String>,Pair<String,String>> getOperationLogMap(Object oldObject, Object newObject) {
        //返回结果集:Map<Pair<最底层class,字段名称>, Pair<旧值,新值>>
        Map<Pair<String,String>, Pair<String,String>> resultMap = new LinkedHashMap<>();
        //返回结果集:Map<Pair<字段路径,最底层class>, 值>
        Map<Pair<String,String>, String> oldResultMap = new LinkedHashMap<>();
        Map<Pair<String,String>, String> newResultMap = new LinkedHashMap<>();
        //如果内容为空则返回空map
        if (ObjectUtils.isEmpty(oldObject) || ObjectUtils.isEmpty(newObject)) {
            return resultMap;
        }
        //如果两个对象Class不一致则提示
        if (!oldObject.getClass().equals(newObject.getClass())) {
            throw new ServiceException(ApiError.Default);
        }
        //旧对象转Map
        Map<String,Object> oldMap = BeanMapUtil.objToMap(oldObject);
        //新对象转Map
        Map<String,Object> newMap = BeanMapUtil.objToMap(newObject);
        //迭代器
        Iterator<Map.Entry<String, Object>> oldIterator = oldMap.size() == 0 ? null : oldMap.entrySet().iterator();
        Iterator<Map.Entry<String, Object>> newIterator = newMap.size() == 0 ? null : newMap.entrySet().iterator();

        //旧数据时记录
        if (ObjectUtils.isNotEmpty(oldIterator)) {
            while (oldIterator .hasNext()){
                Map.Entry oldEntry  =  (java.util.Map.Entry)oldIterator.next();
                String key =  oldEntry.getKey().toString();
                Object value = oldEntry.getValue();
                doOpValue(key,value,oldResultMap,oldObject,null,key);
            }
        }
        //新数据时记录
        if (ObjectUtils.isNotEmpty(newIterator)) {
            while (newIterator .hasNext()){
                Map.Entry newEntry  =  (java.util.Map.Entry)newIterator.next();
                String key =  newEntry.getKey().toString();
                Object value = newEntry.getValue();
                doOpValue(key,value,newResultMap,oldObject,null,key);
            }
        }
      //取两个Map中的并集
        Map<Pair<String,String>, String> map = BeanMapUtil.getUnionSetByGuava(oldResultMap, newResultMap);
       if (ObjectUtils.isNotEmpty(map)) {
           createResultMap(map,oldResultMap,newResultMap,resultMap);
       }
        return resultMap;
    }


    private static void doOpValue(String key,Object value,Map<Pair<String,String>, String> resultMap,Object object,String type,String oldKey) {
        String newKey = key;
        //如果value为空则直接返回
        if (ObjectUtils.isEmpty(value)) {
            return;
        }

        //判断value值的类型
        int objectType = TransitionUtil.getObjectType(value);
        if (objectType == 40) {//判断是否为List
            Field declaredField = null;
            String typeName ="";
            try {
                declaredField = object.getClass().getDeclaredField(newKey);
                declaredField.setAccessible(true);
                Type genericType = declaredField.getGenericType();
                typeName = genericType.getTypeName();
                typeName = typeName.replace("java.util.List<", "");
                typeName = typeName.replace(">","");
            } catch (Exception e) {
                e.printStackTrace();
            }
            List<Object> list = TransitionUtil.transitionType(value, List.class);
            List<String> stringList = new ArrayList<>();
            for (int i = 0; list.size() > i;i++) {
                int objectType1 = TransitionUtil.getObjectType(list.get(i));
                if (objectType1 == 30) {
                    Map map = (Map) list.get(i);
                    Iterator<Map.Entry<String, Object>> iterator = map.size() == 0 ? null : map.entrySet().iterator();
                    while (iterator .hasNext()){
                        newKey = oldKey;
                        Map.Entry entry  =  (java.util.Map.Entry)iterator.next();
                        String k =  entry.getKey().toString();
                        Object v = entry.getValue();
                        newKey = newKey.concat(".").concat(k).concat(String.valueOf(i));
                        doOpValue(newKey,v,resultMap,object,typeName,k);
                    }
                } else if (objectType == 10) {//判断是否是日期
                    Date date = (Date) list.get(i);
                    String newValue = DateFormatUtils.format(date,DateFormatUtils.ISO_DATE_FORMAT.getPattern());
                    stringList.add(newValue);
                } else {
                    String newValue = String.valueOf(value);
                    stringList.add(newValue);
                }
            }
            if (CollectionUtils.isNotEmpty(stringList)) {
                resultMap.put(new Pair<>(newKey,type),String.join(",",stringList));
            }
        } else if (objectType == 30) {//判断是否为Map
            Map map = TransitionUtil.transitionType(value, Map.class);
            try {
                Field declaredField = object.getClass().getDeclaredField(newKey);
                declaredField.setAccessible(true);
                 type = declaredField.getType().toString();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            Iterator<Map.Entry<String, Object>> iterator = map.size() == 0 ? null : map.entrySet().iterator();
            while (iterator .hasNext()){
                newKey = oldKey;
                Map.Entry entry  =  (java.util.Map.Entry)iterator.next();
                String k =  entry.getKey().toString();
                Object v = entry.getValue();
                newKey = newKey.concat(".").concat(k);
                doOpValue(newKey,v,resultMap,object,type,k);
            }
        } else if (objectType == 10) {//判断是否是日期
            Date date = (Date) value;
            String newValue = DateFormatUtils.format(date,DateFormatUtils.ISO_DATE_FORMAT.getPattern());
            resultMap.put(new Pair<>(newKey,type),newValue);
        } else {
            String newValue = String.valueOf(value);
            resultMap.put(new Pair<>(newKey,type),newValue);
        }
    }


    private static void createResultMap( Map<Pair<String,String>, String> map,Map<Pair<String,String>, String> oldMap,Map<Pair<String,String>, String> newMap,Map<Pair<String,String>, Pair<String,String>> resultMap) {
        Iterator<Map.Entry<Pair<String,String>, String>> iterator = map.entrySet().iterator();
        while (iterator .hasNext()){
            Map.Entry entry  =  (java.util.Map.Entry)iterator.next();
            Pair<String,String> pair = (Pair<String, String>) entry.getKey();
            String key = pair.getKey();
            key= Arrays.stream(key.split(".")).reduce((first, second) -> second).orElse("");
            key = key.replace("^[0-9]*$", "");
            String value = pair.getValue();
            String oldValue = oldMap.get(key) == null ? "" : oldMap.get(key);
            String newValue = newMap.get(key) == null ? "" : newMap.get(key);
            if (!oldValue.equals(newValue)) {
                resultMap.put(new Pair<>(key,value),new Pair<>(oldValue,newValue));
            }
        }
    }


}
