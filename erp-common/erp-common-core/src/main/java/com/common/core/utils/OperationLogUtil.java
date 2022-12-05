package com.common.core.utils;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import org.apache.commons.math3.util.Pair;
import org.apache.poi.ss.formula.functions.T;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

        //新增数据时记录
        if (ObjectUtils.isEmpty(oldIterator)) {
            if (ObjectUtils.isNotEmpty(newIterator)) {
                while (newIterator .hasNext()){
                    Map.Entry newEntry  =  (java.util.Map.Entry)newIterator.next();
                    String key =  newEntry.getKey().toString();
                    Object newValue = newEntry.getValue();

                    doOpValue(key,newValue);
                }

            }

        }


        return resultMap;
    }


    private static void doOpValue(String key,Object value) {
        //如果value为空则直接返回
        if (ObjectUtils.isEmpty(value)) {
            return;
        }
        //判断value值的类型
        int objectType = TransitionUtil.getObjectType(value);
        if (objectType == 40) {//判断是否为List
            List<Object> list = TransitionUtil.transitionType(value, List.class);


        } else if (objectType == 20) {

        } else if (objectType == 30) {

        }
    }

}
