package com.common.business.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * @author liuruipeng
 * @date 2024年02月04日 10:00
 */
public class CollectionUtils {

    public static <T> List<T> convertToList(Object input, Class<T> clazz) {
        List<T> resultList = new ArrayList<>();
        if (input instanceof List) {
            List<?> list = (List<?>) input;
            for (Object obj : list) {
                if (clazz.isInstance(obj)) {
                    resultList.add(clazz.cast(obj));
                }
            }
        } else if (clazz.isInstance(input)) {
            resultList.add(clazz.cast(input));
        }

        return resultList;
    }

    public static  List<String> convertStrClzToList(Object input) {
        if(Objects.isNull(input)){
            return new ArrayList<>();
        }
        return CollectionUtils.convertToList(input,String.class);
    }

    public static <T> List<T> paginateList(List<T> list, int pageSize, int pageNum) {
        if(list.isEmpty()){
            return new ArrayList<>();
        }
        // 计算起始索引和结束索引
        int startIndex = (pageNum - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, list.size());

        if(startIndex > endIndex){
            return new ArrayList<>();
        }
        // 获取分页后的子列表
        List<T> pageList = list.subList(startIndex, endIndex);

        // 返回分页后的子列表
        return pageList;
    }
}
