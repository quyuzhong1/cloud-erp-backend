package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ReturnModeEnum {

    DEDUCTION("deduction","退货扣款"),
    REPLENISHMENT("replenishment","退货补货");

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


    ReturnModeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getName(String code) {
        for (ReturnModeEnum returnModeEnum : ReturnModeEnum.values()) {
            if (code.equals(returnModeEnum.getCode())) {
                return returnModeEnum.getName();
            }
        }
        return "";
    }
}
