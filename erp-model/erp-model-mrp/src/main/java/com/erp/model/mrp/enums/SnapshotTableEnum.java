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
    VIRTUAL_INVENTORY("virtual_inventory", "虚拟库存表"),
    PURCHASE_APPLICATION("purchase_application", "采购申请表"),
    PURCHASE_APPLICATION_DETAIL("purchase_application_detail", "采购申请单明细表"),
    PURCHASE_ORDER("purchase_order", "采购订单表"),
    PURCHASE_ORDER_DETAIL("purchase_order_detail", "采购订单明细表"),
    SUBCONTRACT_ORDER("subcontract_order", "委外订单"),
    SUBCONTRACT_ORDER_DETAIL("subcontract_order_detail", "委外订单明细"),
    PURCHASE_APPLICATION_REF_PO("purchase_application_ref_po", "采购申请单和采购订单关联表"),
    TRANSACTION_FLOW("transaction_flow", "库存交易流水表"),
    INSTOCK_FORCAST("instock_forcast", "入库预报表"),
    PO_RECEIVE("po_receive", "仓库签收单"),
    PO_INSTOCK("po_instock", "采购入库单"),
    PO_RETURN("po_return", "采购退货单"),
    TRANSFER_OUT("transfer_out", "分布式调出单"),
    TRANSFER_IN("transfer_in", "分布式调入单"),
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
