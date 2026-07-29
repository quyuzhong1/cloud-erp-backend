package com.erp.model.oms.enums;

import java.util.Objects;

/**
 * 可发货状态枚举
 * 依据锁定数量（冻结数量）与销售数量（发货箱数）的关系判定
 *
 * @author cursor
 */
public enum ShipableStatusEnum {

    /**
     * 无货可发：锁定数量 = 0
     */
    NONE(0, "无货可发"),

    /**
     * 部分可发：0 < 锁定数量 < 销售数量
     */
    PART(1, "部分可发"),

    /**
     * 全量可发：锁定数量 = 销售数量
     */
    ALL(2, "全量可发"),
    ;

    private final Integer code;

    private final String name;

    ShipableStatusEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getName(Integer code) {
        if (code == null) {
            return "";
        }
        for (ShipableStatusEnum statusEnum : ShipableStatusEnum.values()) {
            if (Objects.equals(statusEnum.getCode(), code)) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
