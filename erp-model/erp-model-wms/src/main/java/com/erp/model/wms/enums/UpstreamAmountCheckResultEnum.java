package com.erp.model.wms.enums;

/**
 * 销售出库单与上游 B2C 销售订单金额一致性校验结果。
 */
public enum UpstreamAmountCheckResultEnum {

    /** 通过校验 */
    PASS,

    /** 出库明细价税合计为 0，但上游明细实付/金额非 0 */
    AMOUNT_MISMATCH,

    /** 上游 B2C 明细 Feign 查询失败，需人工核实或稍后重试 */
    UPSTREAM_UNAVAILABLE
}
