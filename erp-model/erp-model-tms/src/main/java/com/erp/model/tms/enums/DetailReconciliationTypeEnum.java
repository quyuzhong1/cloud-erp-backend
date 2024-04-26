package com.erp.model.tms.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 明细对账类型
 */
@Getter
@AllArgsConstructor
public enum DetailReconciliationTypeEnum implements EnumMessage {
    ESTIMATED("estimated", "预估"),
    ACTUAL("actual", "实际"),
    DIFF("diff", "差异"),
    ;
    private final String code;
    private final String name;


    public static DetailReconciliationTypeEnum getByCode(String code) {
        return Arrays.stream(DetailReconciliationTypeEnum.values())
                .filter(e -> e.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }

    public static String getNameByCode(String code) {
        if (null == code) {
            return "";
        }
        DetailReconciliationTypeEnum customsTypeNewEnum = Arrays.stream(DetailReconciliationTypeEnum.values())
                .filter(e -> e.getCode().equals(code))
                .findFirst()
                .orElse(null);
        if (null == customsTypeNewEnum) {
            return "";
        }
        return customsTypeNewEnum.getName();
    }
}