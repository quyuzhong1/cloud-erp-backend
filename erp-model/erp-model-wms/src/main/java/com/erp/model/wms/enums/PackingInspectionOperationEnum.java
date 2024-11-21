package com.erp.model.wms.enums;

import com.common.core.constant.EnumMessage;

/**
 * 包装验货操作枚举
 */
public enum PackingInspectionOperationEnum implements EnumMessage {
    BY_ORDER_AND_SKU("byOrderAndSku", "按单号和sku验货"),
    BY_ORDER("byOrder", "按单号验货")
    ;
    /**
     * 类型
     */
    private String code;
    /**
     * 名称
     */
    private String name;

    PackingInspectionOperationEnum(String code, String name) {
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

}
