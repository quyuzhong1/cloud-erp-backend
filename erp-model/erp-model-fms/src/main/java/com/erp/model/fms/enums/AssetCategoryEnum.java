package com.erp.model.fms.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 资产类别枚举
 * @author system
 * @since 2025-10-23
 */
@Getter
@AllArgsConstructor
public enum AssetCategoryEnum {
    
    /**
     * 机械设备
     */
    MACHINERY("machinery", "机械设备");
    
    /**
     * 类别编码
     */
    private final String code;
    
    /**
     * 类别名称
     */
    private final String name;
    
    /**
     * 根据编码获取枚举
     * @param code 类别编码
     * @return 枚举
     */
    public static AssetCategoryEnum getByCode(String code) {
        if (code == null) {
            return MACHINERY;
        }
        for (AssetCategoryEnum category : values()) {
            if (category.getCode().equals(code)) {
                return category;
            }
        }
        return MACHINERY;
    }
    
    /**
     * 根据编码获取名称
     * @param code 类别编码
     * @return 类别名称
     */
    public static String getName(String code) {
        return getByCode(code).getName();
    }
    
    /**
     * 根据名称获取编码
     * @param name 类别名称
     * @return 类别编码，如果未找到则返回空字符串
     */
    public static String getCodeByName(String name) {
        if (name == null) {
            return "";
        }
        for (AssetCategoryEnum category : values()) {
            if (category.getName().equals(name) || category.getCode().equalsIgnoreCase(name)) {
                return category.getCode();
            }
        }
        return "";
    }
}

