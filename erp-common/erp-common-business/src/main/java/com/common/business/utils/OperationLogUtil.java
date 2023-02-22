package com.common.business.utils;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapUtil;
import com.common.core.utils.TransitionUtil;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.math3.util.Pair;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.*;

/**
 * @author Will
 * @version 1.0
 * @description: 操作日志util
 * @date 2022/12/1 21:14
 */
public class OperationLogUtil {


    /**
     * 根据传入对象生成修改记录Map
     */
    public static Map<Pair<String,String>,Pair<String,String>> getOperationLogMap(Object oldObject, Object newObject) {
        //返回结果集:Map<Pair<最底层class,字段名称>, Pair<旧值,新值>>
        Map<Pair<String,String>, Pair<String,String>> resultMap = new LinkedHashMap<>();
        //返回结果集:Map<字段路径,Pair<,最底层class,值> >
        Map<String,Pair<String,String>> oldResultMap = new LinkedHashMap<>();
        Map<String,Pair<String,String>> newResultMap = new LinkedHashMap<>();
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
                doOpValue(key,value,oldResultMap,oldObject,String.valueOf(oldObject.getClass()),key);
            }
        }
        //新数据时记录
        if (ObjectUtils.isNotEmpty(newIterator)) {
            while (newIterator .hasNext()){
                Map.Entry newEntry  =  (java.util.Map.Entry)newIterator.next();
                String key =  newEntry.getKey().toString();
                Object value = newEntry.getValue();
                doOpValue(key,value,newResultMap,newObject,String.valueOf(newObject.getClass()),key);
            }
        }
      //取两个Map中的并集
        Map<String,Pair<String,String>> map = BeanMapUtil.getUnionSetByGuava(oldResultMap, newResultMap);
       if (ObjectUtils.isNotEmpty(map)) {
           createResultMap(map,oldResultMap,newResultMap,resultMap);
       }
        return resultMap;
    }

    /**
     * 迭代循环类中对象并将值返回到resultMap中
     */
    private static void doOpValue(String key,Object value,Map<String,Pair<String,String>> resultMap,Object object,String type,String oldKey) {
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
                throw new ServiceException(ApiError.Default);
            }
            List<Object> list = TransitionUtil.transitionType(value, List.class);
            List<String> stringList = new ArrayList<>();
            for (int i = 0; list.size() > i;i++) {
                int objectType1 = TransitionUtil.getObjectType(list.get(i));
                if (objectType1 == 30) {//判断是否是Map
                    Map map = (Map) list.get(i);
                    Iterator<Map.Entry<String, Object>> iterator = map.size() == 0 ? null : map.entrySet().iterator();
                    while (iterator .hasNext()){
                        newKey = oldKey;
                        Map.Entry entry  =  (java.util.Map.Entry)iterator.next();
                        String k =  entry.getKey().toString();
                        Object v = entry.getValue();
                        if (!( v instanceof List)) {
                            newKey = newKey.concat(".").concat(k).concat(String.valueOf(i));
                        }
                        doOpValue(newKey,v,resultMap,object,typeName,k);
                    }
                } else if (objectType == 10) {//判断是否是日期
                    Date date = (Date) list.get(i);
                    String newValue = DateFormatUtils.format(date,DateFormatUtils.ISO_DATE_FORMAT.getPattern());
                    stringList.add(newValue);
                } else {
                    String newValue = String.valueOf(list.get(i));
                    stringList.add(newValue);
                }
            }
            if (CollectionUtils.isNotEmpty(stringList)) {
                resultMap.put(newKey,new Pair<>(type,String.join(",",stringList)));
            }
        } else if (objectType == 30) {//判断是否为Map
            Map map = TransitionUtil.transitionType(value, Map.class);
            try {
                Field declaredField = object.getClass().getDeclaredField(newKey);
                declaredField.setAccessible(true);
                type = declaredField.getType().toString();
            } catch (NoSuchFieldException e) {
                throw new ServiceException(ApiError.Default);
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
            resultMap.put(newKey,new Pair<>(type,newValue));
        }  else if (objectType == 50) {//判断是否是BigDecimal类型
            BigDecimal bd = (BigDecimal)value;
            resultMap.put(newKey,new Pair<>(type,bd.stripTrailingZeros().toPlainString()));
        } else {
            String newValue = String.valueOf(value);
            resultMap.put(newKey,new Pair<>(type,newValue));
        }
    }

    /**
     * 处理list中的下标，并生成结果集
     */
    private static void createResultMap( Map<String,Pair<String,String>> map,Map<String,Pair<String,String>> oldMap,Map<String,Pair<String,String>> newMap,Map<Pair<String,String>, Pair<String,String>> resultMap) {
        Iterator<Map.Entry<String,Pair<String,String>>> iterator = map.entrySet().iterator();
        while (iterator .hasNext()){
            Map.Entry entry  =  (java.util.Map.Entry)iterator.next();
            String key = (String) entry.getKey();
            String field= Arrays.stream(key.split("\\.")).reduce((first, second) -> second).orElse("");
            field = field.replace("^[0-9]*$", "");

            Pair<String, String> oldPair = oldMap.get(key);
            Pair<String, String> newPair = newMap.get(key);
            String oldValue = "";
            String newValue = "";
            String path = "";
            if (ObjectUtils.isNotEmpty(oldPair)) {
                oldValue = oldPair.getValue();
                path = oldPair.getKey();
            }
            if (ObjectUtils.isNotEmpty(newPair)) {
                newValue = newPair.getValue();
                path = newPair.getKey();
            }
            if (!oldValue.equals(newValue)) {
                resultMap.put(new Pair<>(field,path),new Pair<>(oldValue,newValue));
            }
        }
    }


}
