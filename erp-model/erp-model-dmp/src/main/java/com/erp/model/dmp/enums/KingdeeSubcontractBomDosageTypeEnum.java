package com.erp.model.dmp.enums;

/**
 * 金蝶委外用料清单-用量类型
 */
public enum KingdeeSubcontractBomDosageTypeEnum {

    FIXED("1", "固定"),
    VARIABLE("2", "变动"),
    ;

    private final String code;
    private final String name;

    KingdeeSubcontractBomDosageTypeEnum(String code, String name) {
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
