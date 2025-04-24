package com.erp.model.mrp.enums;

import com.common.core.constant.EnumMessage;

/**
 * 创建类型
 * @author will
 * @date 2024/9/9 11:54
 */
public enum CreateTypeEnum implements EnumMessage {

    //系统
    AUTO("auto", "系统"),
    //手动
    MANUAL("manual", "人工");


    private String code;
    private String name;

    CreateTypeEnum(String code, String name) {
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

    public static String getNameByCode(String code) {
        CreateTypeEnum[] stateEnums = values();
        for (CreateTypeEnum stateEnum : stateEnums) {
            if (stateEnum.getCode().equals(code) ) {
                return stateEnum.getName();
            }
        }
        return "";
    }
}
