package com.erp.model.wms.enums;

import com.common.core.constant.EnumMessage;

/**
 * B2B三方发货单每箱张贴货件标签数（客户指定装箱/已暂存箱发货时可选值）
 */
public enum B2bLabelsPerBoxEnum implements EnumMessage {

    ONE(1, "每箱1张"),
    TWO(2, "每箱2张"),
    FOUR(4, "每箱4张"),
    ;

    private final Integer code;
    private final String name;

    B2bLabelsPerBoxEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public String getCode() {
        return String.valueOf(code);
    }

    @Override
    public String getName() {
        return name;
    }

    public Integer getValue() {
        return code;
    }

    public static boolean isValid(Integer value) {
        if (value == null) {
            return false;
        }
        for (B2bLabelsPerBoxEnum item : values()) {
            if (item.code.equals(value)) {
                return true;
            }
        }
        return false;
    }
}
