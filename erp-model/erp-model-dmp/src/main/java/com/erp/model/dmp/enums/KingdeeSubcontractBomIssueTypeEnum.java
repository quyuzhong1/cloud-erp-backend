package com.erp.model.dmp.enums;

/**
 * 金蝶委外用料清单-发料方式
 */
public enum KingdeeSubcontractBomIssueTypeEnum {

    DIRECT_PICK("1", "直接领料"),
    DIRECT_BACKFLUSH("2", "直接倒冲"),
    TRANSFER_PICK("3", "调拨领料"),
    TRANSFER_BACKFLUSH("4", "调拨倒冲"),
    NO_ISSUE("7", "不发料"),
    ;

    private final String code;
    private final String name;

    KingdeeSubcontractBomIssueTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static KingdeeSubcontractBomIssueTypeEnum getByCode(String code) {
        for (KingdeeSubcontractBomIssueTypeEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        return null;
    }
}
