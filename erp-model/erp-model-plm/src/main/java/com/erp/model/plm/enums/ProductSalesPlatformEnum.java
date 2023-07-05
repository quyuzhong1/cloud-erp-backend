package com.erp.model.plm.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ProductSalesPlatformEnum implements EnumMessage {
    PLATFORM_ALL("platformAll", "全平台", "全平台", ""),
    AMAZON_CUSTOMIZED("amazonCustomized", "亚马逊定制", "亚马逊定制", ""),
    ;

    @JsonValue
    @EnumValue
    private String code;

    private String name;

    private String desc;

    private String kingdeeCode;

    public String getDesc() {
        return desc;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getKingdeeCode() {
        return kingdeeCode;
    }

    ProductSalesPlatformEnum(String code, String name, String desc, String kingdeeCode) {
        this.code = code;
        this.name = name;
        this.desc = desc;
        this.kingdeeCode = kingdeeCode;
    }

    public static ProductSalesPlatformEnum getByCode(String code) {
        ProductSalesPlatformEnum[] values = values();
        for (ProductSalesPlatformEnum value : values) {
            if (value.code.equals(code) ) {
                return value;
            }
        }
        return null;
    }

    public static String getNameByName(String name) {
        ProductSalesPlatformEnum[] values = values();
        for (ProductSalesPlatformEnum value : values) {
            if (value.name.equals(name)) {
                return value.getName();
            }
        }
        return "";
    }

    public static ProductSalesPlatformEnum getByName(String name) {
        ProductSalesPlatformEnum[] values = values();
        for (ProductSalesPlatformEnum value : values) {
            if (value.name.equals(name)) {
                return value;
            }
        }
        return null;
    }
}
