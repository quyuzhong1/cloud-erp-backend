package com.sdk.wangdian.sdk.api.wms;

/**
 * 旺店通其他出入库 remark 字段约定常量。
 */
public final class WdtOtherStockRemarkConstants {

    /** remark 中携带 ERP 原始单据号的前缀 */
    public static final String SOURCE_CODE_PREFIX = "原始单据号：";

    private WdtOtherStockRemarkConstants() {
    }

    public static String buildSourceCodeRemark(String sourceCode) {
        return SOURCE_CODE_PREFIX + sourceCode;
    }
}
