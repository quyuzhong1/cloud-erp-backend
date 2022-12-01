package com.erp.server.dmp.utils;

import java.util.*;

public class KingdeeUtils {

    /**
     * 列名称转换成map并赋值
     * @param fieldKeys
     * @param list
     */
    public static Map<String, String> keySetValByLinked(String fieldKeys, List<?> list) {
        String[] split = fieldKeys.split(",");
        Map<String, String> map = new LinkedHashMap();
        for (String val : split) {
            map.put(val, null);
        }
        Integer indexSign = 0;
        Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, String> next = iterator.next();
            next.setValue(String.valueOf(list.get(indexSign)));
            indexSign++;
        }
        return map;
    }

    public static void main(String[] args) {
        String fieldKeys = "FID,FBillNo,FDate,FBillTypeID,FDocumentStatus";
        List<String> list = new ArrayList<>();
        list.add("test1");
        list.add("test2");
        list.add("test3");
        list.add("test4");
        list.add("test5");
        Map<String, String> stringStringMap = keySetValByLinked(fieldKeys, list);
        System.out.println(stringStringMap);
    }
}
