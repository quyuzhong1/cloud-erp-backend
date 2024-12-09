package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 重量分摊
 */
public enum WeightAllocationSmallBagEnum implements EnumMessage {
    OUTSTOCK_CHARGED_WEIGHT("outstockChargedWeight","出库计费重分摊"),
    SUPPLIER_CHARGED_WEIGHT("supplierChargedWeight","物流商计费重分摊"),
    SINGLE_PRODUCT_WEIGHT("singleProductWeight","单产品重量分摊"),
    NETWEIGHT("netWeight","出库实重分摊"),
    VOLUMEWEIGHT("volumeWeight","出库体积重分摊"),
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


    WeightAllocationSmallBagEnum(String code, String name) {
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
        for (WeightAllocationSmallBagEnum settingEnum : WeightAllocationSmallBagEnum.values()) {
            if (code.equals(settingEnum.getCode())) {
                return settingEnum.getName();
            }
        }
        return "";
    }

    public static WeightAllocationSmallBagEnum getEnum(String code) {
        for (WeightAllocationSmallBagEnum settingEnum : WeightAllocationSmallBagEnum.values()) {
            if (code.equals(settingEnum.getCode())) {
                return settingEnum;
            }
        }
        return null;
    }
}
