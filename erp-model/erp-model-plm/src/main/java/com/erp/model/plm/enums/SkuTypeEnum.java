package com.erp.model.plm.enums;

/**
 * @Classname SkuTypeEnum
 * SKU数据类型

 * @Date 2025-09-11 10:05
 * @Created  zdy
 */
public enum SkuTypeEnum {
    PLATFORM_SKU_NO("platform_sku_no", "客户SKU"),
    SKU("sku", "SKU"),
    EAN("ean", "EAN");

    private String code;
    private String name;


    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }


    SkuTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getName(String code) {
        for (SkuTypeEnum item : SkuTypeEnum.values()) {
            if (code.equals(item.getCode())) {
                return item.getName();
            }
        }
        return "";
    }

    public static String getCode(String name) {
        for (SkuTypeEnum item : SkuTypeEnum.values()) {
            if (name.equals(item.getName())) {
                return item.getCode();
            }
        }
        return "";
    }
}
