package com.erp.server.dmp.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @Author: wtr
 * @Date: 2025/12/22 16:42
 * @Param:
 * @Return:
 * @Description:
 **/
public enum FbaOutStockTypeEnum implements EnumMessage {

    AWD(1,"awd"),
    STA(0,"sta"),
    ;

    @EnumValue
    @JsonValue
    private int code;
    private String name;

   FbaOutStockTypeEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public Object getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }
}
