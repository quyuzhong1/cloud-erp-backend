package com.erp.sdk.third.kingdee.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class KingdeeUtils {


    public static final String SO_CHANGE_URL="Kingdee.K3.SCM.WebApi.ServicesStub.SaveXSaleOrderWebApi.SaveXSaleOrder";

    /**
     * 金蝶 Save 报 ")"附近有语法错误 的根因：NeedUpDateFields 里列出的字段在 Model 中为空字符串 "" 或占位符 "-" 导致。
     * 仅对「在 NeedUpDateFields 中且 Model 里值为空串或 "-"」的字段做处理：从 Model 移除该 key，并从 NeedUpDateFields 中移除该字段名（不传该字段，金蝶保留原值）。
     *
     * @param model           金蝶 Save 的 Model，会被原地修改
     * @param needUpDateFields NeedUpDateFields 列表，会被原地修改（移除空串/"-"对应的字段名）
     */
    public static void sanitizeModelEmptyStrings(JSONObject model, List<String> needUpDateFields) {
        if (model == null || needUpDateFields == null || needUpDateFields.isEmpty()) {
            return;
        }
        Set<String> needUpDateSet = new HashSet<>(needUpDateFields);
        Set<String> removedKeys = new HashSet<>();
        sanitizeModelEmptyStringsRecurse(model, needUpDateSet, removedKeys);
        needUpDateFields.removeIf(removedKeys::contains);
    }

    /** 判断是否需清洗：空串或 "-" 且在 NeedUpDateFields 中则从 Model 移除 */
    private static boolean isBlankOrPlaceholder(Object val) {
        if (val == null || !(val instanceof CharSequence)) {
            return false;
        }
        String s = val.toString().trim();
        return s.isEmpty() || "-".equals(s);
    }

    private static void sanitizeModelEmptyStringsRecurse(JSONObject node, Set<String> needUpDateSet, Set<String> removedKeys) {
        if (node == null) {
            return;
        }
        List<String> toRemove = new ArrayList<>();
        for (String key : node.keySet()) {
            Object val = node.get(key);
            if (val == null) {
                continue;
            }
            if (isBlankOrPlaceholder(val) && needUpDateSet.contains(key)) {
                toRemove.add(key);
                removedKeys.add(key);
                continue;
            }
            if (val instanceof JSONObject) {
                sanitizeModelEmptyStringsRecurse((JSONObject) val, needUpDateSet, removedKeys);
            } else if (val instanceof JSONArray) {
                JSONArray arr = (JSONArray) val;
                for (int i = 0; i < arr.size(); i++) {
                    Object item = arr.get(i);
                    if (item instanceof JSONObject) {
                        sanitizeModelEmptyStringsRecurse((JSONObject) item, needUpDateSet, removedKeys);
                    }
                }
            }
        }
        for (String key : toRemove) {
            node.remove(key);
        }
    }


    /**
     * 列名称转换成map并赋值
     *
     * @param fieldKeys
     * @param list
     */
    public static Map<String, String> keySetValByLinked(String fieldKeys, List<?> list) {
        String[] split = fieldKeys.split(",");
        Map<String, String> map = new LinkedHashMap();
        for (String val : split) {
            map.put(StrUtil.trim(val), null);
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

    public static Map<String, Object> getApiDataForMap(String fieldKeys, List<?> dataObjects) {
        Map<String, Object> map = new LinkedHashMap();

        String[] fieldKeyArray = fieldKeys.split(",");
        for (String val : fieldKeyArray) {
            map.put(StrUtil.trim(val), null);
        }
        Integer indexSign = 0;
        Iterator<Map.Entry<String, Object>> iterator = map.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, Object> next = iterator.next();
            next.setValue(String.valueOf(dataObjects.get(indexSign)));
            indexSign++;
        }
        return map;
    }

    public static JSONObject makeFieldJson(JSONObject jsonRoot, String pathStr, String splitStr, Object value) {
        if (null == jsonRoot) {
            jsonRoot = new JSONObject();
        }

        String[] fieldArr = pathStr.replace(splitStr, "||").split("\\|\\|");
        JSONObject curNode = jsonRoot;
        for (int i = 0; i < fieldArr.length; i++) {
            String field = fieldArr[i];
            if (i < fieldArr.length - 1) {
                curNode.putIfAbsent(field, new JSONObject());
                curNode = curNode.getJSONObject(field);
            } else {
                curNode.putIfAbsent(fieldArr[fieldArr.length - 1], value);
            }
        }

        return jsonRoot;
    }
}
