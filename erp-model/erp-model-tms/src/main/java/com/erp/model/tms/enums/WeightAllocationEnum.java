package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 重量分摊
 */
public enum WeightAllocationEnum implements EnumMessage {
    OUTSTOCK_CHARGED_WEIGHT("outstockChargedWeight","按照出库计费重"),
    SUPPLIER_CHARGED_WEIGHT("supplierChargedWeight","按照物流商计费重"),
    SINGLE_PRODUCT_WEIGHT("singleProductWeight","按照产品管理-单产品重量"),
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


    WeightAllocationEnum(String code, String name) {
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
        for (WeightAllocationEnum settingEnum : WeightAllocationEnum.values()) {
            if (code.equals(settingEnum.getCode())) {
                return settingEnum.getName();
            }
        }
        return "";
    }

    public static WeightAllocationEnum getEnum(String code) {
        for (WeightAllocationEnum settingEnum : WeightAllocationEnum.values()) {
            if (code.equals(settingEnum.getCode())) {
                return settingEnum;
            }
        }
        return null;
    }
}
