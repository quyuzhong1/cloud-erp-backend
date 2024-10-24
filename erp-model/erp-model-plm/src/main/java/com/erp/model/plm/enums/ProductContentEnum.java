package com.erp.model.plm.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductContentEnum {
    /**
     * SKU
     */
    SKU,
    /**
     * EAN
     */
    EAN,
    /**
     * 品名
     */
    PRODUCT_NAME,
    /**
     * 自定义
     */
    CUSTOM,
    /**
     * MADE_IN_CHINA
     */
    MADE_IN_CHINA,
    /**
     * 日期
     */
    DATE;

}
