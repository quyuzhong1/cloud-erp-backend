package com.erp.server.tms.utils;

/**
 * 报关合并明细默认取值（与产品物流信息为空时的兜底一致）。
 */
public final class DeclareMergeDefaults {

    private DeclareMergeDefaults() {
    }

    /**
     * 境内货源地默认
     */
    public static final String DEFAULT_SOURCE_CARGO = "深圳特区";

    /**
     * 征免默认（照章征税）
     */
    public static final String DEFAULT_EXEMPTION = "照章征税";
}
