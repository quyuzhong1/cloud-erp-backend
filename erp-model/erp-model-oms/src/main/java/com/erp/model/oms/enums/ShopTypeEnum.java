package com.erp.model.oms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;
/**
 * 店铺类型：overseas 海外, internal 国内
 */
public enum  ShopTypeEnum implements EnumMessage{

    OVERSEAS("overseas", "海外"),
    INTERNAL("internal", "国内"),
    ;
    /**
     * 类型
     */
    @EnumValue
    @JsonValue
    private String code;
    private String name;


    ShopTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public String getCode() {
        return code;
    }
    @Override
    public String getName() {
        return name;
    }

    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (ShopTypeEnum billTypeEnum : ShopTypeEnum.values()) {
            if (code.equals(billTypeEnum.getCode())) {
                return billTypeEnum.getName();
            }
        }
        return "";
    }

    public static String getCodeByName(String name) {
        if (StringUtils.isBlank(name)) {
            return "";
        }
        for (ShopTypeEnum billTypeEnum : ShopTypeEnum.values()) {
            if (name.trim().equals(billTypeEnum.getName())) {
                return billTypeEnum.getCode();
            }
        }
        return "";
    }
}
