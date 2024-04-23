package com.common.business.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @Description 电池类型枚举
 */
public enum BatteryTypeEnum implements EnumMessage {

    BUILT_AND_NON_REMOVABLE("builtAndNonRemovable","带电池(内置不可拆卸)"),
    BUILT_AND_REMOVABLE("builtAndRemovable","带电池(内置可拆卸)"),
    PURE_ELECTRICITY("pureElectricity","带电池(纯电)"),
    ;


    @JsonValue
    @EnumValue
    private String code;

    private String name;


    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }


    BatteryTypeEnum(String code, String name){
        this.code = code;
        this.name = name;
    }

}
