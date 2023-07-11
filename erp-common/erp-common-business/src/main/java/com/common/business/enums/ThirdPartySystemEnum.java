package com.common.business.enums;

/**
 * @author Will
 * @version 1.0
 * @description: 第三方平台类型枚举
 * @date 2023/6/29 15:47
 */
public enum ThirdPartySystemEnum {

    ENUM_MB("mb", "马帮平台"),
    ENUM_KINGDEE("kingdee", "金蝶");

    private String code;
    private String msg;

    ThirdPartySystemEnum(String code, String msg) {
        this.code = code;
        this.msg = msg;

    }

    public String getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
