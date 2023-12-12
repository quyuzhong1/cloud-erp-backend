package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Will
 * @version 1.0
 * @description: 运费模板类型枚举
 * @date 2023/11/6 16:39
 */
public enum ShippingTemplateTypeEnum implements EnumMessage {

    ENUM_COUNTRY("country","按国家/地区"),
    ENUM_REGION("region","按国家/地区+分区"),
    ENUM_WAREHOUSE("warehouse","按仓库")
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

    ShippingTemplateTypeEnum(String code, String name){
        this.code = code;
        this.name = name;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getName() {
        return this.name;
    }


    public static ShippingTemplateTypeEnum getEnumByCode(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        for (ShippingTemplateTypeEnum typeEnums : ShippingTemplateTypeEnum.values()) {
            if (code.equals(typeEnums.getCode())) {
                return typeEnums;
            }
        }
        return null;
    }

    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (ShippingTemplateTypeEnum typeEnums : ShippingTemplateTypeEnum.values()) {
            if (code.equals(typeEnums.getCode())) {
                return typeEnums.getName();
            }
        }
        return "";
    }
}
