package com.common.business.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * 发票类型
 *
 * @author Lambda
 * @Classname InvoiceTypeEnum

 * @Date 2023-05-12 9:12
 * @Created by yl
 */
public enum InvoiceTypeEnum {

    VALUE_ADDED_TAX("valueAddedTax", "增值税发票", "增值税发票"),
    INVOICE("invoice", "普通发票", "普通发票");
    private String code;

    private String name;

    private String desc;

    InvoiceTypeEnum(String code, String name, String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (InvoiceTypeEnum statusEnum : InvoiceTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }


}
