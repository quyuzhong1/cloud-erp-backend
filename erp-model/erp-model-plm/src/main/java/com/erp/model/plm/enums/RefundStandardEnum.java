package com.erp.model.plm.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RefundStandardEnum implements EnumMessage {
    PURCHASE_ORDERS("purchase_orders", "采购下单"),
    RECEIVING("receiving", "采购收货"),
    IN_STOCK("in_stock", "采购入库");

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
