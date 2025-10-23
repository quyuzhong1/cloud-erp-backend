package com.erp.model.fms.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 计量单位枚举
 * @author system
 * @since 2025-10-23
 */
@Getter
@AllArgsConstructor
public enum UnitEnum {
    
    /**
     * PCS
     */
    PCS("pcs", "PCS");
    
    /**
     * 单位编码
     */
    private final String code;
    
    /**
     * 单位名称
     */
    private final String name;
    
    /**
     * 根据编码获取枚举
     * @param code 单位编码
     * @return 枚举
     */
    public static UnitEnum getByCode(String code) {
        if (code == null) {
            return PCS;
        }
        for (UnitEnum unit : values()) {
            if (unit.getCode().equals(code)) {
                return unit;
            }
        }
        return PCS;
    }
    
    /**
     * 根据编码获取名称
     * @param code 单位编码
     * @return 单位名称
     */
    public static String getName(String code) {
        return getByCode(code).getName();
    }
}

