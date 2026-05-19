package com.common.business.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
public enum AfterSalePackTypeEnum {

    PRODUCT_RENOVATE("productRenovate", "良品翻新"),
    PURCHASE_RETURN("purchaseReturn", "采购退供"),
    TRANSFER("transfer", "调拨"),
    UNKNOWN("unknown", "未知");

    private final String code;
    private final String name;

    AfterSalePackTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getByName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (AfterSalePackTypeEnum item : AfterSalePackTypeEnum.values()) {
            if (code.equals(item.getCode())) {
                return item.getName();
            }
        }
        return "";
    }
}
