package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum  ReturnTypeEnum {

    DEDUCTION("refund","退货退款"),
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


    ReturnTypeEnum(String code, String name) {
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
