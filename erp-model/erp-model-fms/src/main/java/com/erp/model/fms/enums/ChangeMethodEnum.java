package com.erp.model.fms.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 变动方式枚举
 * @author system
 * @since 2025-10-23
 */
@Getter
@AllArgsConstructor
public enum ChangeMethodEnum {
    
    /**
     * 购入
     */
    PURCHASE("purchase", "购入"),

    /**
     * 盘盈
     */
    PROFIT("profit", "盘盈");
    
    /**
     * 变动方式编码
     */
    private final String code;
    
    /**
     * 变动方式名称
     */
    private final String name;
    
    /**
     * 根据编码获取枚举
     * @param code 变动方式编码
     * @return 枚举
     */
    public static ChangeMethodEnum getByCode(String code) {
        if (code == null) {
            return PURCHASE;
        }
        for (ChangeMethodEnum method : values()) {
            if (method.getCode().equals(code)) {
                return method;
            }
        }
        return PURCHASE;
    }
    
    /**
     * 根据编码获取名称
     * @param code 变动方式编码
     * @return 变动方式名称
     */
    public static String getName(String code) {
        return getByCode(code).getName();
    }
    
    /**
     * 根据名称获取编码
     * @param name 变动方式名称
     * @return 变动方式编码，如果未找到则返回空字符串
     */
    public static String getCodeByName(String name) {
        if (name == null) {
            return "";
        }
        for (ChangeMethodEnum method : values()) {
            if (method.getName().equals(name) || method.getCode().equalsIgnoreCase(name)) {
                return method.getCode();
            }
        }
        return "";
    }
}

