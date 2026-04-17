package com.erp.model.dmp.enums;


import com.common.core.constant.EnumMessage;
import lombok.Getter;

@Getter
public enum InventoryBillStatusEnum implements EnumMessage {
    /**
     * 待处理
     */
    INIT("init", "待处理"),
    /**
     * 成功
     */
    SUCCESS("success", "成功"),
    /**
     * 失败
     */
    FAILED("failed", "失败");

    private final String code;
    private final String name;

    InventoryBillStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static InventoryBillStatusEnum fromCode(String code) {
        for (InventoryBillStatusEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }
}