package com.erp.model.oms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author Lambda
 * @Classname DictEnum
 * @Description TODO
 * @Date 2023-08-21 14:15
 * @Created by yl
 */
public enum PlatformDictEnum implements EnumMessage {
    OFFLINECELEBRITY("Offlinecelebrity","线下发网红","023"),
    SHOPIFY("Shopify","shopify",""),
    AMAZON("Amazon","亚马逊","");
    ;

    PlatformDictEnum(String code, String name,String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }
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

    private String desc;
    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }
}
