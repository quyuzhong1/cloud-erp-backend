package com.common.business.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
        return CollectionUtils.convertToList(input,String.class);
    }
}
