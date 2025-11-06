package com.erp.model.fms.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 折旧费枚举
 * @author system
 * @since 2025-10-23
 */
@Getter
@AllArgsConstructor
public enum DepreciationChargeEnum {
    
    /**
     * 费用项目
     */
    COST_TYPE("costType", "费用项目");
    
    /**
     * 折旧费编码
     */
    private final String code;
    
    /**
     * 折旧费名称
     */
    private final String name;
    
    /**
     * 根据编码获取枚举
     * @param code 折旧费编码
     * @return 枚举
     */
    public static DepreciationChargeEnum getByCode(String code) {
        if (code == null) {
            return COST_TYPE;
        }
        for (DepreciationChargeEnum charge : values()) {
            if (charge.getCode().equals(code)) {
                return charge;
            }
        }
        return COST_TYPE;
    }
    
    /**
     * 根据编码获取名称
     * @param code 折旧费编码
     * @return 折旧费名称
     */
    public static String getName(String code) {
        return getByCode(code).getName();
    }
    
    /**
     * 根据名称获取编码
     * @param name 折旧费名称
     * @return 折旧费编码，如果未找到则返回空字符串
     */
    public static String getCodeByName(String name) {
        if (name == null) {
            return "";
        }
        for (DepreciationChargeEnum charge : values()) {
            if (charge.getName().equals(name) || charge.getCode().equalsIgnoreCase(name)) {
                return charge.getCode();
            }
        }
        return "";
    }
}

