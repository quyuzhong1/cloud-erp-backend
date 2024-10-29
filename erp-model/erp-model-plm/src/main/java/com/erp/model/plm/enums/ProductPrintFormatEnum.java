package com.erp.model.plm.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductPrintFormatEnum {
    /**
     * 分开打印
     */
    SEPARATELY_SKU_EAN,
    /**
     * 只打印sku
     */
    SKU,
    /**
     * 只打印EAN
     */
    EAN,
    /**
     * 合并打印
     */
    MERGE_SKU_EAN;

}
