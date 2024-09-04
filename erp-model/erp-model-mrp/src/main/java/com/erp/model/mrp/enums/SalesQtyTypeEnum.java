package com.erp.model.mrp.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SalesQtyTypeEnum implements EnumMessage {
    BY_CREATE_TIME("byCreateTime","以销售订单订单创建时间计算销量"),
    BY_OUT_STOCK_TIME("byOutStockTime","以销售出库单出库时间计算销量")
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
