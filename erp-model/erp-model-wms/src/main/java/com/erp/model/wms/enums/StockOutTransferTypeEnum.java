package com.erp.model.wms.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 出库中转配置类型枚举
 * @date 2024-07-16
 * @author tanmujin
 */
@Getter
@AllArgsConstructor
public enum StockOutTransferTypeEnum implements EnumMessage {
    B2B("B2B", "B2B"),
    B2C("B2C", "B2C"),
    FIRST_MILE("firstMile", "头程"),
    ;
    private String code;
    private String name;
}
