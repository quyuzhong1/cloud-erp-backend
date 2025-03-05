package com.erp.model.sys.enums;

import cn.hutool.core.text.CharSequenceUtil;

/**
 * @description: 数据权限类型
 * @author Will
 * @date: 2024/5/23 15:27
 */
public enum AuthDataTypeEnum {

    ENUM_ALL("all",  "全部"),
    ENUM_PART("part",  "部分"),
    ;


    private String code;
    private String name;


    AuthDataTypeEnum(String code, String name) {

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
        for (AuthDataTypeEnum statusEnum : AuthDataTypeEnum.values()) {
            if (CharSequenceUtil.equals(statusEnum.getCode(),code)) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
