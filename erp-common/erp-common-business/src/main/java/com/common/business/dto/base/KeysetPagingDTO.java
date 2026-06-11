package com.common.business.dto.base;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 键集分页请求（导出等场景）：{@code WHERE id > lastId ORDER BY id LIMIT limit}
 */
@Data
public class KeysetPagingDTO<P> {

    /**
     * limit 缺省/非法时的默认单批条数
     */
    public static final int DEFAULT_LIMIT = 5000;

    /**
     * 上一批最大 id，首轮为 null 表示从头扫描
     */
    private Long lastIdExclusive;

    private Integer limit = DEFAULT_LIMIT;

    @NotNull
    private P params;

    /**
     * 执行键集分页查询前对 {@code limit} 做兜底校验，返回安全的单批拉取条数：
     * <ul>
     *     <li>下界：小于 1 或为空时退回 {@link #DEFAULT_LIMIT}</li>
     *     <li>上界：clamp 到 {@code maxLimit}，防止调用方传入超大 limit 导致单次拉数过多引发 Feign 超时与导出 OOM</li>
     * </ul>
     * 上界为配置驱动，由调用方在查询时传入（例如 erp-server-file 的 {@code FileRegistry.getMaxPageSize()}，
     * 对应配置 {@code file.storage.maxPageSize}）；本模块不直接依赖该配置，故不在字段上硬编码 {@code @Max}。
     *
     * @param maxLimit 配置化的单批最大条数上限；{@code <= 0} 时退回 {@link #DEFAULT_LIMIT}
     * @return 介于 {@code [1, maxLimit]} 的安全 limit
     */
    public int resolveSafeLimit(int maxLimit) {
        int max = maxLimit <= 0 ? DEFAULT_LIMIT : maxLimit;
        int current = (limit == null || limit < 1) ? DEFAULT_LIMIT : limit;
        return Math.min(current, max);
    }
}
