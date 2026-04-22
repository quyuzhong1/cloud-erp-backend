package com.erp.model.dmp.enums;


import com.common.core.constant.EnumMessage;
import lombok.Getter;

@Getter
public enum InventorySyncModeEnum implements EnumMessage {
    /**
     * 库存同步
     */
    INVENTORY("inventory", "库存同步"),
    /**
     * 单据同步
     */
    ORDER("order", "单据同步");

    private final String code;
    private final String name;

    InventorySyncModeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static InventorySyncModeEnum fromCode(String code) {
        for (InventorySyncModeEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }
}