package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum PoReceiveSourceTypeEnum implements EnumMessage {
    DELIVERY_ORDER("deliveryOrder","发货单"),
    ;

    PoReceiveSourceTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
    /**
     * 类型
     */
    @EnumValue
    @JsonValue
    private String code;
    /**
     * 名称
     */
    private String name;
}
