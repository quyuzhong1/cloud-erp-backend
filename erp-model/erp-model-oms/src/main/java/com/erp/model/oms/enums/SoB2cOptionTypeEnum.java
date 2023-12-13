package com.erp.model.oms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Will
 * @version 1.0
 * @description: B2C销售订单操作类型枚举
 * @date 2023/8/22 12:27
 */
public enum SoB2cOptionTypeEnum {

    ENUM_MERGE("merge",  "合并"),
    ENUM_SPLIT("split",  "拆分"),

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


    SoB2cOptionTypeEnum(String code, String name) {
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
        for (SoB2cOptionTypeEnum soB2cOptionTypeEnum : SoB2cOptionTypeEnum.values()) {
            if (code.equals(soB2cOptionTypeEnum.getCode())) {
                return soB2cOptionTypeEnum.getName();
            }
        }
        return "";
    }
}
