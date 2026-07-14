package com.erp.model.dmp.enums;

/**
 * 金蝶库存状态枚举
 */
public enum KingdeeStockStatusEnum {

    USABLE("KCZT01_SYS", "可用"),
    DEFECTIVE("KCZT08_SYS", "不良品"),
    ;

    private final String code;
    private final String name;

    KingdeeStockStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
