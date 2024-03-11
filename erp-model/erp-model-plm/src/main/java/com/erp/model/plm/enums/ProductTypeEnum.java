package com.erp.model.plm.enums;

import com.common.core.constant.EnumMessage;

/**
 * @author Will
 * @version 1.0

 * @date 2022/12/12 13:15
 */
public enum ProductTypeEnum implements EnumMessage {

    NEW_PRODUCT("newProduct","新产品"),

    ITERATIVE_PRODUCT("iterativeProduct","迭代产品");

    private String code;

    private String name;


    ProductTypeEnum(String code, String name) {
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
        for (ProductTypeEnum state : ProductTypeEnum.values()) {
            if (code.equals(state.getCode())) {
                return state.getName();
            }
        }
        return "";
    }
}
