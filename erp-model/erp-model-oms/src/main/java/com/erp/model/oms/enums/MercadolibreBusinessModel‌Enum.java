package com.erp.model.oms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

public enum MercadolibreBusinessModel‌Enum {
    CBT("CBT", "全球站"),
    BR("BR", "巴西（本土）"),
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


    MercadolibreBusinessModel‌Enum(String code, String name) {
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
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (MercadolibreBusinessModel‌Enum bankTypeEnum : MercadolibreBusinessModel‌Enum.values()) {
            if (code.equals(bankTypeEnum.getCode())) {
                return bankTypeEnum.getName();
            }
        }
        return "";
    }
}
