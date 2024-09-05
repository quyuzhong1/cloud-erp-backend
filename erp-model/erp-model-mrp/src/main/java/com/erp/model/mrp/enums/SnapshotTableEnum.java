package com.erp.model.mrp.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SnapshotTableEnum implements EnumMessage {

    FBA_INVENTORY("fba_inventory", "FBA库存"),
    FBA_SHIPMENT("fba_shipment", "FBA货件表"),
    FBA_SHIPMENT_DETAIL("fba_shipment_detail", "FBA货件明细表"),
    FIRST_MILE_DELIVERY("first_mile_delivery", "发货单"),
    FIRST_MILE_DELIVERY_DETAIL("first_mile_delivery_detail", "发货单明细"),
    LOGISTICS_BILL("logistics_bill", "物流单"),
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
