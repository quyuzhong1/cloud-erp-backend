package com.erp.model.fms.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 处置情况枚举
 * @author system
 * @since 2025-10-23
 */
@Getter
@AllArgsConstructor
public enum DisposalStatusEnum {
    
    /**
     * 部分处理
     */
    PARTIAL("partial", "部分处理"),
    
    /**
     * 完全清理
     */
    COMPLETE("complete", "完全清理");
    
    /**
     * 处置状态编码
     */
    private final String code;
    
    /**
     * 处置状态名称
     */
    private final String name;
    
    /**
     * 根据编码获取枚举
     * @param code 处置状态编码
     * @return 枚举
     */
    public static DisposalStatusEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (DisposalStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
    
    /**
     * 根据编码获取名称
     * @param code 处置状态编码
     * @return 处置状态名称
     */
    public static String getName(String code) {
        DisposalStatusEnum status = getByCode(code);
        return status != null ? status.getName() : "";
    }
}

