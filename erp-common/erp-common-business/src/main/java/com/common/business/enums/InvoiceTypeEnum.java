package com.common.business.enums;

/**
 * 发票类型
 *
 * @author Lambda
 * @Classname InvoiceTypeEnum
 * @Description TODO
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


}
