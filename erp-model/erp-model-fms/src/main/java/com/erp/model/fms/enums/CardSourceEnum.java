package com.erp.model.fms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.NoArgsConstructor;

/**
 * 资产卡片来源枚举
 * 
 * @author wuht
 * @date 2025-01-10
 */
@NoArgsConstructor
public enum CardSourceEnum {
    
    /**
     * 采购收货
     */
    PURCHASE_RECEIPT("purchaseReceipt", "采购收货"),
    
    /**
     * 手工建卡
     */
    MANUAL_CREATE("manualCreate", "手工建卡"),
    
    /**
     * 盘盈建卡
     */
    INVENTORY_SURPLUS("inventorySurplus", "盘盈建卡");
    
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
    
    CardSourceEnum(String code, String name) {
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
        for (CardSourceEnum cardSourceEnum : CardSourceEnum.values()) {
            if (cardSourceEnum.getCode().equals(code)) {
                return cardSourceEnum.getName();
            }
        }
        return "";
    }
    
    /**
     * 根据SourceTypeEnum的code映射到CardSourceEnum
     * @param sourceTypeCode SourceTypeEnum的code
     * @return CardSourceEnum的code
     */
    public static String mapFromSourceType(String sourceTypeCode) {
        if (sourceTypeCode == null) {
            return MANUAL_CREATE.getCode();
        }
        
        // 根据SourceTypeEnum映射到CardSourceEnum
        switch (sourceTypeCode) {
            case "assetAcceptance":
                return PURCHASE_RECEIPT.getCode();
            case "inventoryGainLoss":
                return INVENTORY_SURPLUS.getCode();
            default:
                return MANUAL_CREATE.getCode();
        }
    }
    
    /**
     * 根据SourceTypeEnum的code获取CardSourceEnum的名称
     * @param sourceTypeCode SourceTypeEnum的code
     * @return CardSourceEnum的名称
     */
    public static String getNameFromSourceType(String sourceTypeCode) {
        String cardSourceCode = mapFromSourceType(sourceTypeCode);
        return getName(cardSourceCode);
    }
}