package com.erp.model.fms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.NoArgsConstructor;

/**
 * 盘盈盘亏单据类型枚举
 * 
 * @author wuht
 * @date 2025-10-31
 */
@NoArgsConstructor
public enum AssetProfitLossTypeEnum {
    
    /**
     * 盘盈
     */
    PROFIT("profit", "盘盈"),
    
    /**
     * 盘亏
     */
    LOSS("loss", "盘亏");
    
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
    
    AssetProfitLossTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getName() {
        return name;
    }
    
    /**
     * 根据code获取名称
     */
    public static String getName(String code) {
        for (AssetProfitLossTypeEnum typeEnum : AssetProfitLossTypeEnum.values()) {
            if (typeEnum.getCode().equals(code)) {
                return typeEnum.getName();
            }
        }
        return "";
    }
    
    /**
     * 根据code获取枚举
     */
    public static AssetProfitLossTypeEnum getByCode(String code) {
        for (AssetProfitLossTypeEnum typeEnum : AssetProfitLossTypeEnum.values()) {
            if (typeEnum.getCode().equals(code)) {
                return typeEnum;
            }
        }
        return null;
    }
}

