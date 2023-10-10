package com.erp.model.sys.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;

public enum SysTypeEnum {
    ALL("ALL","全部"),
    PC("PC","PC端"),
    PDA("PDA","PDA"),
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

    SysTypeEnum(String code, String name) {
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
        for (SysTypeEnum sysTypeEnum : SysTypeEnum.values()) {
            if (code.equals(sysTypeEnum.getCode())) {
                return sysTypeEnum.getName();
            }
        }
        return "";
    }

    public static String getCodeByName(String name) {
        for (SysTypeEnum sysTypeEnum : SysTypeEnum.values()) {
            if(Objects.equals(name, sysTypeEnum.name)) {
                return sysTypeEnum.code;
            }
        }
        return "";
    }

}
