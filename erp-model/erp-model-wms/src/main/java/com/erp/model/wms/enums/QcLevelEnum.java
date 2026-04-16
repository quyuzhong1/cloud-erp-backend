package com.erp.model.wms.enums;

import com.common.core.constant.EnumMessage;
import lombok.Getter;

/**
 * 检验水平枚举（GB/T2828.1-2012）
 */
@Getter
public enum QcLevelEnum implements EnumMessage {
    S1("S-1", "特殊(S-1)"),
    S2("S-2", "特殊(S-2)"),
    S3("S-3", "特殊(S-3)"),
    S4("S-4", "特殊(S-4)"),
    I("I", "一般(I)"),
    II("II", "一般(II)"),
    III("III", "一般(III)");

    private final String code;
    private final String name;

    QcLevelEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    // 根据编码获取枚举
    public static QcLevelEnum getByCode(String code) {
        for (QcLevelEnum level : values()) {
            if (level.getCode().equals(code)) {
                return level;
            }
        }
        return null;
    }
}
