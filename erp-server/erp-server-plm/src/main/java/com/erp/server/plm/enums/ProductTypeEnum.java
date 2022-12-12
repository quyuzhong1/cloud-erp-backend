package com.erp.server.plm.enums;

import com.common.core.constant.EnumMessage;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/12 13:15
 */
public enum ProductTypeEnum implements EnumMessage {

    NEWPRODUCT(1,"新产品"),

    ITERATIVEPRODUCT(2,"迭代产品");

    private Integer code;

    private String name;


    ProductTypeEnum(Integer colourState, String name) {
        this.code = colourState;
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getName(Integer code) {
        for (ProductTypeEnum state : ProductTypeEnum.values()) {
            if (code.equals(state.getCode())) {
                return state.getName();
            }
        }
        return "";
    }
}
