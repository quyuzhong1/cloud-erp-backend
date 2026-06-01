package com.erp.model.oms.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 寄样费用成本来源
 */
@Getter
@AllArgsConstructor
public enum KolSampleCostCostSourceEnum implements EnumMessage {

    SKU_COST("skuCost", "SKU成本"),
    PURCHASE_AVG_COST("purchaseAvgCost", "采购平均成本"),
    ;

    private final String code;

    private final String name;
}
