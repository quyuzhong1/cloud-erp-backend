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
    SO_B2C("so_b2c", "B2C销售订单表"),
    SO_B2C_DETAIL("so_b2c_detail", "B2C销售订单明细表"),
    SO_OUT_STOCK("so_outstock", "销售订单出库单"),
    SO_OUT_STOCK_DETAIL("so_outstock_detail", "销售订单出库明细"),
    WMS_DELIVERY_PLAN("wms_delivery_plan", "发货计划"),
    WMS_DELIVERY_PLAN_DETAIL("wms_delivery_plan_detail", "发货计划详情表"),
    OVERSEAS_INVENTORY("overseas_inventory", "海外仓库存"),
    INVENTORY("inventory", "库存表"),
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

    public static String getTableName(SnapshotTableEnum snapshotTable, String calcDate) {
        return snapshotTable.getCode() + "_" + calcDate;
    }
}
