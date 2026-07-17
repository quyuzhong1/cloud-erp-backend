package com.erp.server.dmp.utils;

import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;

/**
 * DMP handler 内部用于把 mongo 原始数据填充到 dmp 数据 Map 的工具方法集合。
 * <p>
 * 历史上 {@link com.erp.server.dmp.inout.handler.input.task.dmp.DmpInputDbConvertDmpHandler}
 * 的多个子类各自重复实现了相同的 {@code putStringIfNotBlank} / {@code putJsonIfPresent} 逻辑，
 * 集中到这里以避免分散维护、字段映射变更漏改。
 *
 * @author Administrator
 */
public final class DmpFieldMapUtils {

    private DmpFieldMapUtils() {
    }

    /**
     * 将原始 value 转字符串后写入 dmpDataMap：
     * - value 为 {@code null} 直接跳过；
     * - {@link Object#toString()} 后空白字符串跳过；
     * - 否则以原始字符串落库。
     */
    public static void putStringIfNotBlank(TreeMap<String, Object> dmpDataMap, String key, Object value) {
        if (value == null) {
            return;
        }
        String str = value.toString();
        if (StringUtils.isNotBlank(str)) {
            dmpDataMap.put(key, str);
        }
    }

    /**
     * 将 source 中 sourceKey 对应的对象按 JSON 形式写入 dmpDataMap 的 targetKey：
     * - source 为 {@code null} 或不含 sourceKey 跳过；
     * - value 为 {@code null} 跳过；
     * - value 为 String 时按非空白原样写入；
     * - 其它类型统一 {@link JSON#toJSONString(Object)} 序列化后写入。
     */
    public static void putJsonIfPresent(TreeMap<String, Object> dmpDataMap, String targetKey,
                                        Map<String, Object> source, String sourceKey) {
        if (source == null || !source.containsKey(sourceKey)) {
            return;
        }
        Object value = source.get(sourceKey);
        if (value == null) {
            return;
        }
        if (value instanceof String) {
            if (StringUtils.isNotBlank((String) value)) {
                dmpDataMap.put(targetKey, value);
            }
            return;
        }
        dmpDataMap.put(targetKey, JSON.toJSONString(value));
    }
}
