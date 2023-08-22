package com.erp.model.oms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Will
 * @version 1.0
 * @description: B2C销售订单分类类型枚举
 * @date 2023/8/22 12:27
 */
public enum B2cSoCategoryTypeEnum {

    ENUM_ADD("add",  "新增"),
    ENUM_UPDATE("update",  "修改"),
    ENUM_DELETE("delete",  "删除"),

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


    B2cSoCategoryTypeEnum(String code, String name) {
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
        for (B2cSoCategoryTypeEnum b2cSoCategoryTypeEnum : B2cSoCategoryTypeEnum.values()) {
            if (code.equals(b2cSoCategoryTypeEnum.getCode())) {
                return b2cSoCategoryTypeEnum.getName();
            }
        }
        return "";
    }
}
