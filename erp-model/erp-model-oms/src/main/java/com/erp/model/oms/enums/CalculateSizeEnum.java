package com.erp.model.oms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * 物流尺寸长宽高枚举
 */
public enum CalculateSizeEnum {

    LENGTH("length", "长度"),
    WIDTH("width", "宽度"),
    HEIGHT("height", "高度"),
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


    CalculateSizeEnum(String code, String name) {
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
        for (CalculateSizeEnum bankTypeEnum : CalculateSizeEnum.values()) {
            if (code.equals(bankTypeEnum.getCode())) {
                return bankTypeEnum.getName();
            }
        }
        return "";
    }
}
