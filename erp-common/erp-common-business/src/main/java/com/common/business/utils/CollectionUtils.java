package com.common.business.utils;

import java.util.*;
import java.util.stream.Collectors;

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

    // 用于比较两个 List 是否一致，忽略顺序但考虑重复元素
    public static boolean areListsEqualWithFrequency(List<String> list1, List<String> list2) {
        if(org.apache.commons.collections4.CollectionUtils.isEmpty(list1) && org.apache.commons.collections4.CollectionUtils.isEmpty(list2)){
            return true;
        }
        if(org.apache.commons.collections4.CollectionUtils.isEmpty(list1) || org.apache.commons.collections4.CollectionUtils.isEmpty(list2)){
            return false;
        }
        // 如果两个列表的大小不一致，直接返回 false
        if (list1.size() != list2.size()) {
            return false;
        }

        // 将两个列表转换为 Map<String, Long>，统计每个元素的出现次数
        Map<String, Long> freqMap1 = list1.stream()
                .collect(Collectors.groupingBy(e -> e, Collectors.counting()));

        Map<String, Long> freqMap2 = list2.stream()
                .collect(Collectors.groupingBy(e -> e, Collectors.counting()));

        // 比较两个频率 Map 是否相等
        return freqMap1.equals(freqMap2);
    }
}
