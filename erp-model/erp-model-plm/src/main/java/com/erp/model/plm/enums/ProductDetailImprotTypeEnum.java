package com.erp.model.plm.enums;

import com.common.core.constant.EnumMessage;

/**
 * @Date 2025-107-28
 * @Created jack
 */
public enum ProductDetailImprotTypeEnum implements EnumMessage {

    ADD("add","新增更新"),
    REPLACE("replace","替换更新"),
    ;

    private String code;

    private String name;


    ProductDetailImprotTypeEnum(String code, String name) {
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
        for (ProductDetailImprotTypeEnum state : ProductDetailImprotTypeEnum.values()) {
            if (code.equals(state.getCode())) {
                return state.getName();
            }
        }
        return "";
    }

    public static ProductDetailImprotTypeEnum getEnum(String code) {
        for (ProductDetailImprotTypeEnum state : ProductDetailImprotTypeEnum.values()) {
            if (code.equals(state.getCode())) {
                return state;
            }
        }
        return null;
    }
}
