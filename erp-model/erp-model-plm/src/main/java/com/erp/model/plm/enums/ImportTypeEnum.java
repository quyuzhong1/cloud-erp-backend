package com.erp.model.plm.enums;

import com.common.core.constant.EnumMessage;

/**
 * @Classname  立项状态

 * @Date 2022-09-29 14:13
 * @Created by yl
 */
public enum ImportTypeEnum implements EnumMessage {

    IMPORT_ADD(1,"导入新增"),
    IMPORT_NOT_APPROVAL(2,"导入未审核"),
    IMPORT_APPROVAL(3,"导入审核");


    private Integer code;

    private String name;


    ImportTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }



    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getName(Integer code) {
        for (ImportTypeEnum state : ImportTypeEnum.values()) {
            if (code.equals(state.getCode())) {
                return state.getName();
            }
        }
        return "";
    }

    public static ImportTypeEnum getEnum(Integer code) {
        for (ImportTypeEnum state : ImportTypeEnum.values()) {
            if (code.equals(state.getCode())) {
                return state;
            }
        }
        return null;
    }
}
