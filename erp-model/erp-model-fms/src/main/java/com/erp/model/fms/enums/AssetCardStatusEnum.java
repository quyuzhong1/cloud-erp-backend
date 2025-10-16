package com.erp.model.fms.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 资产卡片关联状态枚举
 * @author wuht
 * @since 2025-10-13
 */
@Getter
@AllArgsConstructor
public enum AssetCardStatusEnum {
    
    /**
     * 未生成
     */
    NOT_GENERATED("not_generated", "未生成"),
    
    /**
     * 已生成
     */
    GENERATED("generated", "已生成");
    
    /**
     * 状态码
     */
    private final String status;
    
    /**
     * 状态名称
     */
    private final String name;
    
    /**
     * 根据状态码获取枚举
     * @param status 状态码
     * @return 枚举
     */
    public static AssetCardStatusEnum getByStatus(String status) {
        if (status == null) {
            return NOT_GENERATED;
        }
        for (AssetCardStatusEnum statusEnum : values()) {
            if (statusEnum.getStatus().equals(status)) {
                return statusEnum;
            }
        }
        return NOT_GENERATED;
    }
    
    /**
     * 根据状态码获取状态名称
     * @param status 状态码
     * @return 状态名称
     */
    public static String getName(String status) {
        return getByStatus(status).getName();
    }
    
    /**
     * 获取所有状态列表
     * @return 状态列表
     */
    public static String[] getStatusList() {
        return new String[]{NOT_GENERATED.getStatus(), GENERATED.getStatus()};
    }
}
