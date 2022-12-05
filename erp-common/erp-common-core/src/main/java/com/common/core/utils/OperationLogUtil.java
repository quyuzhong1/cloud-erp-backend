package com.common.core.utils;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.core.utils.date.DateUtil;
import com.common.core.utils.date.EnumTimePattern;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.math3.util.Pair;
import org.apache.poi.ss.formula.functions.T;

import java.lang.reflect.Field;
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

    public static void main(String[] args) {

        getOperationLogMap(new OperationLogUtil(),new OperationLogUtil());
    }


    public static Map<String,Pair<String,String>> getOperationLogMap(Object oldObject, Object newObject) {
        //返回结果集:Map<字段, Pair<旧值,新值>>
        Map<String, Pair<String,String>> resultMap = new LinkedHashMap<>();

        Map<String, String> oldResultMap = new LinkedHashMap<>();
        Map<String, String> newResultMap = new LinkedHashMap<>();
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
                doOpValue(key,value,oldResultMap);
            }
        }
        //新数据时记录
        if (ObjectUtils.isNotEmpty(newIterator)) {
            while (newIterator .hasNext()){
                Map.Entry newEntry  =  (java.util.Map.Entry)newIterator.next();
                String key =  newEntry.getKey().toString();
                Object value = newEntry.getValue();
                doOpValue(key,value,newResultMap);
            }
        }
      //取两个Map中的交集
        Map<String, String> map1 = BeanMapUtil.getUnionSetByGuava(oldResultMap, newResultMap);
        //取就Map中的差集
        Map<String, String> map2 = BeanMapUtil.getDifferenceSetByGuava(oldResultMap, newResultMap);
       if (ObjectUtils.isNotEmpty(map1)) {
           Iterator<Map.Entry<String, String>> iterator = map1.entrySet().iterator();
           while (iterator .hasNext()){
               Map.Entry oldEntry  =  (java.util.Map.Entry)iterator.next();
               String key = (String) oldEntry.getKey();
               String k = Arrays.stream(key.split(".")).reduce((first, second) -> second).orElse(null);
               String oldValue = (String) (oldEntry.getValue() == null ? "" : oldEntry.getValue());
               String newValue = newResultMap.get(key) == null ? "" : newResultMap.get(key);
               if (!oldValue.equals(newValue)) {
                   resultMap.put(k,new Pair<>(oldValue,newValue));
               }
           }
       }
        if (ObjectUtils.isNotEmpty(map2)) {
            Iterator<Map.Entry<String, String>> iterator = map2.entrySet().iterator();
            while (iterator .hasNext()){
                Map.Entry oldEntry  =  (java.util.Map.Entry)iterator.next();
                String key = (String) oldEntry.getKey();
                String k = Arrays.stream(key.split(".")).reduce((first, second) -> second).orElse(null);
                String oldValue = (String) (oldEntry.getValue() == null ? "" : oldEntry.getValue());
                String newValue = newResultMap.get(key) == null ? "" : newResultMap.get(key);
                if (!oldValue.equals(newValue)) {
                    resultMap.put(k,new Pair<>(oldValue,newValue));
                }
            }
        }

        return resultMap;
    }


    private static void doOpValue(String key,Object value,Map<String, String> resultMap) {
        String newKey = key;
        //如果value为空则直接返回
        if (ObjectUtils.isEmpty(value)) {
            return;
        }
        //判断value值的类型
        int objectType = TransitionUtil.getObjectType(value);
        if (objectType == 40) {//判断是否为List
            List<Object> list = TransitionUtil.transitionType(value, List.class);
            for (Object obj:list) {
                int type = TransitionUtil.getObjectType(obj);
                if (type == 30) {//当数据类型为map时
                    Map map = (Map) obj;
                    Iterator<Map.Entry<String, Object>> iterator = map.size() == 0 ? null : map.entrySet().iterator();
                    while (iterator .hasNext()){
                        Map.Entry entry  =  (java.util.Map.Entry)iterator.next();
                        String k =  entry.getKey().toString();
                        Object v = entry.getValue();
                        newKey = newKey.concat(".").concat(k);
                        doOpValue(newKey,v,resultMap);
                    }
                } else {//不是为map则是集合
                    resultMap.put(newKey,String.format(",",obj));
                }
            }

        } else if (objectType == 30) {//判断是否为Map
            Map map = TransitionUtil.transitionType(value, Map.class);
            Iterator<Map.Entry<String, Object>> iterator = map.size() == 0 ? null : map.entrySet().iterator();
            while (iterator .hasNext()){
                Map.Entry entry  =  (java.util.Map.Entry)iterator.next();
                String k =  entry.getKey().toString();
                Object v = entry.getValue();
                newKey = newKey.concat(".").concat(k);
                doOpValue(k,v,resultMap);
            }
        } else if (objectType == 10) {//判断是否是日期
            Date date = (Date) value;
            String newValue = DateFormatUtils.format(date,DateFormatUtils.ISO_DATE_FORMAT.getPattern());
            resultMap.put(newKey,newValue);
        } else {
            String newValue = String.valueOf(value);
            resultMap.put(newKey,newValue);
        }
    }





}
