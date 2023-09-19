package com.erp.model.bi.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DateSalesTrendSearchTypeEnum implements EnumMessage {
    SALES_AMOUNT("salesAmount","销售额"),
    SALES_QUANTITY("salesQuantity","销量"),
    FINANCE_SALES_QUANTITY("financeSalesQuantity","财务销售额"),
    SALES_PRICE("salesPrice","客单价"),
    ;

    DateSalesTrendSearchTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 类型
     */
    @EnumValue
    @JsonValue
    public String code;
    /**
     * 名称
     */
    private String name;

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }


    public static String getNameByCode(String code) {
        DateSalesTrendSearchTypeEnum[] enums = values();
        for (DateSalesTrendSearchTypeEnum typeEnum : enums) {
            if (typeEnum.getCode().equals(code)) {
                return typeEnum.getName();
            }
        }
        return null;
    }

    public static DateSalesTrendSearchTypeEnum getEnumByCode(String code) {
        DateSalesTrendSearchTypeEnum[] enums = values();
        for (DateSalesTrendSearchTypeEnum typeEnum : enums) {
            if (typeEnum.getCode().equals(code)) {
                return typeEnum;
            }
        }
        return null;
    }
}
