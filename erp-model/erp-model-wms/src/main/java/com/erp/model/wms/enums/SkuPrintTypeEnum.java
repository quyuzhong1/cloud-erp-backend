package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * SKU打印样式
 * @Auther zdy
 * @Date 2025/6/12 16:13
 */
public enum SkuPrintTypeEnum implements EnumMessage {

    BARCODE("productBarcode","产品条码"),
    BARCODE_INFO("productBarcodeInfo","产品条码+产品信息"),
    ;

    /**
     * 类型
     */
    @EnumValue
    @JsonValue
    private String code;
    /**
     * 名称
     */
    private String name;


    SkuPrintTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    public static String getName(String code) {
        for (SkuPrintTypeEnum settingEnum : SkuPrintTypeEnum.values()) {
            if (code.equals(settingEnum.getCode())) {
                return settingEnum.getName();
            }
        }
        return "";
    }

    public static SkuPrintTypeEnum getEnum(String code) {
        for (SkuPrintTypeEnum settingEnum : SkuPrintTypeEnum.values()) {
            if (code.equals(settingEnum.getCode())) {
                return settingEnum;
            }
        }
        return null;
    }
}
