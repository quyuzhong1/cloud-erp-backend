package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author Lambda
 * @Classname DeliverTypeEnum
 * @Description TODO
 * @Date 2023-12-29 10:30
 * @Created by yl
 */
public enum DeliverTypeEnum implements EnumMessage {
    MANUAL("manual","手动发货"),
    FALSEHOOD("falsehood","虚假发货"),
    ;

    DeliverTypeEnum(String code, String name) {
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

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getName() {
        return this.name;
    }
}
