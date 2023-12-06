package com.common.business.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 海外仓入库单提交状态
 */
@Getter
@AllArgsConstructor
public enum OverseasVerifyEnum {
    // 入库单创建时取0，发货单审核通过更新为1
    INIT("0", "入库单创建"),
    PASS("1", "发货单审核通过");

    private final String code;
    private final String name;

}