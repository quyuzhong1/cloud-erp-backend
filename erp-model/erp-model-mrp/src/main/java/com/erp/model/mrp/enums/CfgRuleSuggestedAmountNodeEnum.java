package com.erp.model.mrp.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CfgRuleSuggestedAmountNodeEnum implements EnumMessage {
    SUGGESTED_TIME_FRAME("SUGGESTED_TIME_FRAME","建议时间范围"),
    SUGGESTED_DELIVERY_VOLUME_AGING("SUGGESTED_DELIVERY_VOLUME_AGING","建议发货量-时效"),
    SUGGESTED_DELIVERY_VOLUME_INVENTORY("SUGGESTED_DELIVERY_VOLUME_INVENTORY","建议发货量-库存"),
    SUGGESTED_PURCHASE_VOLUME_AGING("SUGGESTED_PURCHASE_VOLUME_AGING","建议采购量-时效"),
    SUGGESTED_PURCHASE_VOLUME_INVENTORY("SUGGESTED_PURCHASE_VOLUME_INVENTORY","建议采购量-库存"),
    ;

    private final String code;

    private final String name;

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }
}
