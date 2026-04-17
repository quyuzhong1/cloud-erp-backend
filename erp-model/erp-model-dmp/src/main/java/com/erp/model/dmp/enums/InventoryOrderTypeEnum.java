package com.erp.model.dmp.enums;


import com.common.core.constant.EnumMessage;
import lombok.Getter;

@Getter
public enum InventoryOrderTypeEnum implements EnumMessage {
    /**
     * 库存同步
     */
    IN_STOCK("inStock", "入库单"),
    /**
     * 单据同步
     */
    OUT_STOCK("outStock", "出库单");

    private final String code;
    private final String name;

    InventoryOrderTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static InventoryOrderTypeEnum fromCode(String code) {
        for (InventoryOrderTypeEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }
}