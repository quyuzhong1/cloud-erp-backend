package com.common.core.utils;

import java.util.*;

public class JsonFieldDiffUtil {

    private JsonFieldDiffUtil() {
    }
    /**
     * 默认忽略的字段列表
     * - id: 通常是主键，不需要比较
     * - create_user_id: 创建用户ID，通常不需要比较
     * - create_user_name: 创建用户名称，通常不需要比较
     * - create_time: 创建时间，通常不需要比较
     * - version: 版本号，通常不需要比较
     */
    private static final Set<String> DEFAULT_IGNORED_FIELDS = new HashSet<>(Arrays.asList(
        "id", "create_user_id", "create_user_name", "create_time","update_user_id", "update_user_name", "update_time", "version","db","P_TAG_IUD","table","schema","P_TAG_CTS","P_TAG_BATCHNO","cdc_kafka_key_rc"
    ));

    /**
     * 比较两个 Map 对象的字段差异，并打印出变化的字段、仅在 after 中存在的字段和仅在 before 中存在的字段。
     * 默认忽略一些常见的字段，如 id、创建用户信息等。
     * @param before       变更前的 Map
     * @param after        变更后的 Map
     */
    public static List<String> compare(Map<String, Object> before, Map<String, Object> after) {
        return compare(before, after, DEFAULT_IGNORED_FIELDS);
    }

    /**
     * 比较两个 Map 对象的字段差异，并打印出变化的字段、仅在 after 中存在的字段和仅在 before 中存在的字段。
     * 默认忽略一些常见的字段，如 id、创建用户信息等。
     * @param before       变更前的 Map
     * @param after        变更后的 Map
     * @param ignoreFields       需要忽略的字段列表
     */
    public static List<String> compare(Map<String, Object> before, Map<String, Object> after, Set<String> ignoreFields) {
        Set<String> allKeys = new HashSet<>();
        if (before != null) allKeys.addAll(before.keySet());
        if (after != null) allKeys.addAll(after.keySet());

        List<String> changedFields = new ArrayList<>();

        for (String key : allKeys) {
            if (null != ignoreFields && ignoreFields.contains(key)) continue;

            Object beforeVal = before.getOrDefault(key,null);
            Object afterVal = after.getOrDefault(key,null);
            if (!Objects.equals(beforeVal, afterVal)) {
                changedFields.add(key);
            }
        }

        return changedFields;
    }
}
