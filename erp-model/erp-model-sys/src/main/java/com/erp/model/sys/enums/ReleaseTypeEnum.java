package com.erp.model.sys.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;

public enum ReleaseTypeEnum {
    //PDA升级通知
    PDA_UPGRADE("0", "升级通知"),
    PDA_SYSTEM("1", "系统通知"),
    //系统通知
    SYS_SYSTEM("sys","系统通知"),
    ;
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

    ReleaseTypeEnum(String code, String name) {
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
        for (ReleaseTypeEnum releaseTypeEnum : ReleaseTypeEnum.values()) {
            if (code.equals(releaseTypeEnum.getCode())) {
                return releaseTypeEnum.getName();
            }
        }
        return "";
    }

    public static ReleaseTypeEnum getReleaseTypeEnum(String code) {
        for (ReleaseTypeEnum releaseTypeEnum : ReleaseTypeEnum.values()) {
            if (code.equals(releaseTypeEnum.getCode())) {
                return releaseTypeEnum;
            }
        }
        return null;
    }

    public static String getCodeByName(String name) {
        for (ReleaseTypeEnum releaseTypeEnum : ReleaseTypeEnum.values()) {
            if(Objects.equals(name, releaseTypeEnum.name)) {
                return releaseTypeEnum.code;
            }
        }
        return "";
    }

}