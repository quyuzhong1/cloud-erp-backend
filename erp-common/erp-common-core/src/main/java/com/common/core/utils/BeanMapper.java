package com.common.core.utils;

import com.github.dozermapper.core.DozerBeanMapperBuilder;
import com.github.dozermapper.core.Mapper;

import java.util.ArrayList;
import java.util.List;

/**
 * @Classname BeanMapper
 * @Description TODO
 * @Date 2022-09-15 16:01
 * @Created by yl
 */
public class BeanMapper {
    private static Mapper dozerBeanMapper =  DozerBeanMapperBuilder.buildDefault();

    public static void copy(Object source, Object destinationObject) {
        if (source != null) {
            dozerBeanMapper.map(source, destinationObject);
        }
    }


    public static <T> List<T> copyList(Iterable<?> sourceList, Class<T> destinationClass) {
        List<T> destinationList = new ArrayList();
        for (Object sourceObject : sourceList) {
            T destinationObject = dozerBeanMapper.map(sourceObject, destinationClass);
            destinationList.add(destinationObject);
        }
        return destinationList;
    }
}
