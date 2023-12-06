package com.erp.model.wms.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 报关方式
 */
@Getter
@AllArgsConstructor
public enum OverseasCustomsTypeNewEnum {
    AGENCY_CUSTOMS_DECLARATION("tradeAgency", "贸易代理报关"),
    REFUND_CUSTOMS_DECLARATION("taxRefund", "退税报关"),
    SELF_CUSTOMS_DECLARATION("self", "报关自理");
    private final String code;
    private final String name;


    public static OverseasCustomsTypeNewEnum getByCode(String code) {
        return Arrays.stream(OverseasCustomsTypeNewEnum.values())
                .filter(e -> e.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }

    public static String getNameByCode(String code) {
        if (null == code) {
            return "";
        }
        OverseasCustomsTypeNewEnum customsTypeNewEnum = Arrays.stream(OverseasCustomsTypeNewEnum.values())
                .filter(e -> e.getCode().equals(code))
                .findFirst()
                .orElse(null);
        if (null == customsTypeNewEnum) {
            return "";
        }
        return customsTypeNewEnum.getName();
    }
}