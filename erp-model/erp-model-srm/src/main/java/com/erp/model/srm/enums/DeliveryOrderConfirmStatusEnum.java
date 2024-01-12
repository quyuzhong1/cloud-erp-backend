package com.erp.model.srm.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 采购退货单确认状态
 */
public enum DeliveryOrderConfirmStatusEnum implements EnumMessage {
    WAIT_CONFIRM("waitConfirm", "待确认"),
    CONFIRM("confirm", "已确认"),
    ;

    @EnumValue
    @JsonValue
    private String code;
    private String name;

    DeliveryOrderConfirmStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }
}
