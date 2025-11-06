package com.erp.model.fms.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 费用项目枚举
 * @author system
 * @since 2025-10-23
 */
@Getter
@AllArgsConstructor
public enum CostTypeEnum {
    
    /**
     * 折旧费
     */
    DEPRECIATION_CHARGE("depreciationCharge", "折旧费");
    
    /**
     * 费用项目编码
     */
    private final String code;
    
    /**
     * 费用项目名称
     */
    private final String name;
    
    /**
     * 根据编码获取枚举
     * @param code 费用项目编码
     * @return 枚举
     */
    public static CostTypeEnum getByCode(String code) {
        if (code == null) {
            return DEPRECIATION_CHARGE;
        }
        for (CostTypeEnum charge : values()) {
            if (charge.getCode().equals(code)) {
                return charge;
            }
        }
        return DEPRECIATION_CHARGE;
    }
    
    /**
     * 根据编码获取名称
     * @param code 费用项目编码
     * @return 费用项目名称
     */
    public static String getName(String code) {
        return getByCode(code).getName();
    }
    
    /**
     * 根据名称获取编码
     * @param name 费用项目名称
     * @return 费用项目编码，如果未找到则返回空字符串
     */
    public static String getCodeByName(String name) {
        if (name == null) {
            return "";
        }
        for (CostTypeEnum charge : values()) {
            if (charge.getName().equals(name) || charge.getCode().equalsIgnoreCase(name)) {
                return charge.getCode();
            }
        }
        return "";
    }
}

