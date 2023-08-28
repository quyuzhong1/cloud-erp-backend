package com.common.core.utils;

import org.apache.poi.ss.formula.functions.T;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author Lambda
 * @Classname FilterUtil
 * @Description TODO
 * @Date 2023-08-22 9:55
 * @Created by yl
 */
public class FilterUtil {


    public static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor){
        Map<Object,Boolean> seen=new ConcurrentHashMap<>();
        return object->seen.putIfAbsent (keyExtractor.apply(object), Boolean.TRUE) == null;
    }


}
