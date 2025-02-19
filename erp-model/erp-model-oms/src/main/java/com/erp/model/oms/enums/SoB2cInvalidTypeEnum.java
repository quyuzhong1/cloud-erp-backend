package com.erp.model.oms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Will
 * @version 1.0
 * @description: B2C销售订单作废类型枚举
 * @date 2023/8/22 12:27
 */
public enum SoB2cInvalidTypeEnum {
    //订单作废：人工操作
    ENUM_MANUAL("manual",  "手工作废"),
    //合并[订单合并]
    ENUM_MERGE("merge",  "合并作废"),
    //拆分[订单拆分，按仓库拆分，捆绑拆分]
    ENUM_SPLIT("split",  "拆分作废"),
    //系统自动作废
    ENUM_AUTOMATIC("automatic",  "自动作废"),

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


    SoB2cInvalidTypeEnum(String code, String name) {
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
        for (SoB2cInvalidTypeEnum soB2cInvalidTypeEnum : SoB2cInvalidTypeEnum.values()) {
            if (code.equals(soB2cInvalidTypeEnum.getCode())) {
                return soB2cInvalidTypeEnum.getName();
            }
        }
        return "";
    }
}
