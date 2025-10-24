package com.erp.model.fms.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 资产状态枚举
 * @author system
 * @since 2025-10-23
 */
@Getter
@AllArgsConstructor
public enum AssetStatusEnum {
    
    /**
     * 正常使用
     */
    NORMAL("normal", "正常使用");
    
    /**
     * 状态编码
     */
    private final String code;
    
    /**
     * 状态名称
     */
    private final String name;
    
    /**
     * 根据编码获取枚举
     * @param code 状态编码
     * @return 枚举
     */
    public static AssetStatusEnum getByCode(String code) {
        if (code == null) {
            return NORMAL;
        }
        for (AssetStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return NORMAL;
    }
    
    /**
     * 根据编码获取名称
     * @param code 状态编码
     * @return 状态名称
     */
    public static String getName(String code) {
        return getByCode(code).getName();
    }
}

