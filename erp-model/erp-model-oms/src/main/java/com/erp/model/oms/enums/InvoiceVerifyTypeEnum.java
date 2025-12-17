package com.erp.model.oms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;

public enum InvoiceVerifyTypeEnum {
    NONE("","NONE"),
    IE("ie","IE"),
    ;
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


    InvoiceVerifyTypeEnum(String code, String name) {
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
        for (InvoiceVerifyTypeEnum addressTypeEnum : InvoiceVerifyTypeEnum.values()) {
            if (code.equals(addressTypeEnum.getCode())) {
                return addressTypeEnum.getName();
            }
        }
        return "";
    }

    public static String getCodeByName(String name) {
        for (InvoiceVerifyTypeEnum addressTypeEnum : InvoiceVerifyTypeEnum.values()) {
            if(Objects.equals(name, addressTypeEnum.name)) {
                return addressTypeEnum.code;
            }
        }
        return "";
    }

}
