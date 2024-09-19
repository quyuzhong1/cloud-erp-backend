package com.erp.model.tms.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 对账类型
 */
@Getter
@AllArgsConstructor
public enum ReconciliationBillTypeEnum implements EnumMessage {
    ESTIMATED("estimated", "预估账单"),
    ACTUAL("actual", "实际账单")
    ;
    private final String code;
    private final String name;


    public static ReconciliationBillTypeEnum getByCode(String code) {
        return Arrays.stream(ReconciliationBillTypeEnum.values())
                .filter(e -> e.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }

    public static String getNameByCode(String code) {
        if (null == code) {
            return "";
        }
        ReconciliationBillTypeEnum customsTypeNewEnum = Arrays.stream(ReconciliationBillTypeEnum.values())
                .filter(e -> e.getCode().equals(code))
                .findFirst()
                .orElse(null);
        if (null == customsTypeNewEnum) {
            return "";
        }
        return customsTypeNewEnum.getName();
    }
}