package com.erp.model.dmp.enums;

/**
 * 金蝶委外用料清单-倒冲时机
 */
public enum KingdeeSubcontractBomBackFlushTypeEnum {

    REPORT_BACKFLUSH("2", "汇报倒冲"),
    INSTOCK_BACKFLUSH("3", "入库倒冲"),
    ;

    private final String code;
    private final String name;

    KingdeeSubcontractBomBackFlushTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static KingdeeSubcontractBomBackFlushTypeEnum getByCode(String code) {
        for (KingdeeSubcontractBomBackFlushTypeEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        return null;
    }
}
