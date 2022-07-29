package com.comm.core.enums;

/**
 * @Classname ThirdpartyPlatform
 * @Description TODO
 * @Date 2022-07-13 17:12
 * @Created by yl
 */
public enum ThirdpartyPlatformEnum {

    FS("FS", "飞书平台"),
    DD("DD", "钉钉平台"),
    QYWX("QYWX", "企业微信平台");

    private String code;
    private String msg;

    ThirdpartyPlatformEnum(String code, String msg) {
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
