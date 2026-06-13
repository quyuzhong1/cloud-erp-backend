package com.erp.server.tms.engine;

import com.common.core.exception.ServiceException;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * TMS 异步任务唯一键生成工具
 *
 * 职责：
 *  - 任务级防重键：buildTaskUniqueKey(...) 系列方法
 *  - 明细级防重键：buildDetailUniqueKey(...)
 *
 * 唯一键文本归一化规则：
 *  - 多字段：按 key=value&key=value 升序拼接
 *  - ID 集合：元素排序后逗号拼接
 *  - Map：key 升序排序后按 key=value&... 拼接
 *  - 空或无参数：使用 "default"
 */
public final class TmsAsyncTaskUniqueKeyGenerator {

    private TmsAsyncTaskUniqueKeyGenerator() {}

    // -------------------------------------------------------
    // 任务级唯一键（任务 3.2 + 3.3）
    // -------------------------------------------------------

    /**
     * 无额外参数时使用默认键（对应 "default" 文本）
     */
    public static TaskUniqueKey buildDefault() {
        return TaskUniqueKey.ofDefault();
    }

    /**
     * 单个字段值作为唯一键文本
     */
    public static TaskUniqueKey buildFromValue(String value) {
        if (StringUtils.isBlank(value)) {
            return TaskUniqueKey.ofDefault();
        }
        return TaskUniqueKey.of(value.trim());
    }

    /**
     * 多字段组合：按 key 升序排列后拼接为 key1=v1&key2=v2
     * 传入 TreeMap 可确保 key 顺序稳定，此方法也接受普通 Map 并内部排序。
     */
    public static TaskUniqueKey buildFromMap(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return TaskUniqueKey.ofDefault();
        }
        String text = new TreeMap<>(params).entrySet().stream()
                .filter(e -> StringUtils.isNotBlank(e.getKey()))
                .map(e -> e.getKey() + "=" + (e.getValue() == null ? "" : e.getValue().trim()))
                .collect(Collectors.joining("&"));
        if (StringUtils.isBlank(text)) {
            return TaskUniqueKey.ofDefault();
        }
        return TaskUniqueKey.of(text);
    }

    /**
     * ID 集合：排序后逗号拼接（适用于按一批 ID 触发的任务）
     */
    public static TaskUniqueKey buildFromIds(Collection<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return TaskUniqueKey.ofDefault();
        }
        String text = new TreeSet<>(ids).stream()
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining(","));
        if (StringUtils.isBlank(text)) {
            return TaskUniqueKey.ofDefault();
        }
        return TaskUniqueKey.of(text);
    }

    /**
     * 多段字符串组合：按固定顺序拼接，用于参数顺序由调用者保证的场景
     * 例如：buildFromParts("2026-05", "FIRST_MILE", "pushAllocation")
     */
    public static TaskUniqueKey buildFromParts(String... parts) {
        if (parts == null || parts.length == 0) {
            return TaskUniqueKey.ofDefault();
        }
        List<String> segments = new ArrayList<>();
        for (String part : parts) {
            if (StringUtils.isNotBlank(part)) {
                segments.add(part.trim());
            }
        }
        if (segments.isEmpty()) {
            return TaskUniqueKey.ofDefault();
        }
        return TaskUniqueKey.of(String.join(":", segments));
    }

    // -------------------------------------------------------
    // 明细级唯一键（任务 3.4）
    // -------------------------------------------------------

    /**
     * 标准明细唯一键：methodType:businessId
     * 同一任务下每条业务明细的最小防重粒度
     *
     * @throws ServiceException 若 methodType 或 businessId 为空——空键会与其他明细碰撞，必须拒绝
     */
    public static String buildDetailUniqueKey(String methodType, String businessId) {
        return buildDetailUniqueKeyWithExtra(methodType, businessId, null);
    }

    /**
     * 带额外参数的明细唯一键：methodType:businessId:extraParam
     * 当同一 businessId 在不同维度下会产生多条独立明细时使用（如费用类型拆分）
     *
     * @throws ServiceException 若 methodType 或 businessId 为空
     */
    public static String buildDetailUniqueKeyWithExtra(String methodType, String businessId, String extraParam) {
        if (StringUtils.isBlank(methodType)) {
            throw new ServiceException("detail_unique_key 生成失败：methodType 不能为空");
        }
        if (StringUtils.isBlank(businessId)) {
            throw new ServiceException("detail_unique_key 生成失败：businessId 不能为空");
        }
        StringBuilder sb = new StringBuilder();
        sb.append(methodType.trim()).append(":").append(businessId.trim());
        if (StringUtils.isNotBlank(extraParam)) {
            sb.append(":").append(extraParam.trim());
        }
        return sb.toString();
    }
}
