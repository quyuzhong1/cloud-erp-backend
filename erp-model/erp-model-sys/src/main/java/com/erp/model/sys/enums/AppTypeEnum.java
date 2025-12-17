package com.erp.model.sys.enums;

/**
 * 应用类型枚举
 *
 * @author system
 * @since 2025-09-18
 */
public enum AppTypeEnum {

    /**
     * 飞书
     */
    FS("FS", "飞书"),

    /**
     * PDA
     */
    PDA("PDA", "PDA"),

    /**
     * 微信小程序
     */
    WECHAT_MINIPROGRAM("WECHAT_MINIPROGRAM", "微信小程序"),

    /**
     * ERP系统
     */
    ERP("ERP", "ERP系统");

    private final String code;
    private final String description;

    AppTypeEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据代码获取枚举
     */
    public static AppTypeEnum getByCode(String code) {
        for (AppTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 根据描述获取枚举
     */
    public static AppTypeEnum getByDescription(String description) {
        for (AppTypeEnum type : values()) {
            if (type.getDescription().equals(description)) {
                return type;
            }
        }
        return null;
    }
}