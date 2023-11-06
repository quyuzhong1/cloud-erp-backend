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
public enum SoB2cCategoryTypeEnum {

    ENUM_ADD("add",  "绑定分类"),
    ENUM_DELETE("delete",  "解绑分类"),
    ENUM_UPDATE("update",  "重置分类"),
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


    SoB2cCategoryTypeEnum(String code, String name) {
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
        for (SoB2cCategoryTypeEnum soB2cCategoryTypeEnum : SoB2cCategoryTypeEnum.values()) {
            if (code.equals(soB2cCategoryTypeEnum.getCode())) {
                return soB2cCategoryTypeEnum.getName();
            }
        }
        return "";
    }
}
