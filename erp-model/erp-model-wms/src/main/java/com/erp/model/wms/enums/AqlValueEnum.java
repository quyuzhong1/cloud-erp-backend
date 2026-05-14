package com.erp.model.wms.enums;

import lombok.Getter;

/**
 * AQL值枚举（国标支持的所有值）
 */
@Getter
public enum AqlValueEnum {
    AQL_0010("0.010"),
    AQL_0015("0.015"),
    AQL_0025("0.025"),
    AQL_0040("0.040"),
    AQL_0065("0.065"),
    AQL_010("0.10"),
    AQL_015("0.15"),
    AQL_025("0.25"),
    AQL_040("0.40"),
    AQL_065("0.65"),
    AQL_10("1.0"),
    AQL_15("1.5"),
    AQL_25("2.5"),
    AQL_40("4.0"),
    AQL_65("6.5"),
    AQL_100("10"),
    AQL_150("15"),
    AQL_250("25"),
    AQL_400("40"),
    AQL_650("65"),
    AQL_1000("100");

    private final String value;

    AqlValueEnum(String value) {
        this.value = value;
    }

    // 校验是否为合法AQL值
    public static boolean isValidAql(String aqlValue) {
        for (AqlValueEnum aql : values()) {
            if (aql.getValue().equals(aqlValue)) {
                return true;
            }
        }
        return false;
    }
}