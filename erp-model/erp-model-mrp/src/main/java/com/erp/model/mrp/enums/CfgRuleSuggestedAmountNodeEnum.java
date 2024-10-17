package com.erp.model.mrp.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CfgRuleSuggestedAmountNodeEnum implements EnumMessage {


    SUGGESTED_STOCK_UP("SUGGESTED_STOCK_UP","建议备货"),
    SUGGESTED_PURCHASE("SUGGESTED_PURCHASE","建议采购"),
    SUGGESTED_TIME("SUGGESTED_TIME","建议时间"),

    SUGGESTED_SHIPPING_QTY("SUGGESTED_SHIPPING_QTY","建议发货量"),
    SUGGESTED_PURCHASE_QTY("SUGGESTED_PURCHASE_QTY","建议采购量"),
    TIME_FRAME("TIME_FRAME","建议时间范围"),

    AGING("AGING","时效"),
    INVENTORY("INVENTORY","库存"),
    PURCHASE_APPROVE_DAYS("PURCHASE_APPROVE_DAYS","审批时长"),
    PRODUCTION_DAYS("PRODUCTION_DAYS","生产周期"),
    SUPPLIER_DELIVERY_DAYS("SUPPLIER_DELIVERY_DAYS","供应商发货时长"),
    QC_DAYS("QC_DAYS","质检入库天数"),
    PURCHASE_CYCLE_DAYS("PURCHASE_CYCLE_DAYS","采购频率"),
    DELIVERY_DAYS("DELIVERY_DAYS","本地发FBA时效"),
    IN_STOCK_DAYS("IN_STOCK_DAYS","FBA入库时间"),
    LOGISTICS_CYCLE_DAYS("LOGISTICS_CYCLE_DAYS","本地仓发货频率"),
    SAFE_DAYS("SAFE_DAYS","FBA安全天数"),
    FBA_USABLE_QTY("FBA_USABLE_QTY","FBA可用"),
    FBA_IN_TRANSIT_QTY("FBA_IN_TRANSIT_QTY","FBA在途"),
    FBA_PLAN_DELIVERY_QTY("FBA_PLAN_DELIVERY_QTY","FBA预计发货"),
    OVERSEAS_USABLE_QTY("OVERSEAS_USABLE_QTY","海外仓可用"),
    OVERSEAS_IN_TRANSIT_QTY("OVERSEAS_IN_TRANSIT_QTY","海外仓在途"),
    OVERSEAS_PLAN_DELIVERY_QTY("OVERSEAS_PLAN_DELIVERY_QTY","海外仓预计发货"),
    LOCAL_USABLE_QTY("LOCAL_USABLE_QTY","本地仓可用"),
    LOCAL_IN_TRANSIT_QTY("LOCAL_IN_TRANSIT_QTY","本地在途"),
    LOCAL_PLAN_PURCHASE_QTY("LOCAL_PLAN_PURCHASE_QTY","预计采购"),
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

    public static String getDeliveryVolumeAging() {
        return String.join(":", SUGGESTED_STOCK_UP.getCode(), SUGGESTED_SHIPPING_QTY.getCode(), AGING.getCode());
    }

    public static String getDeliveryVolumeInventory() {
        return String.join(":", SUGGESTED_STOCK_UP.getCode(), SUGGESTED_SHIPPING_QTY.getCode(), INVENTORY.getCode());
    }

    public static String getPurchaseVolumeAging() {
        return String.join(":", SUGGESTED_STOCK_UP.getCode(), SUGGESTED_PURCHASE_QTY.getCode(), AGING.getCode());
    }

    public static String getPurchaseVolumeInventory() {
        return String.join(":", SUGGESTED_STOCK_UP.getCode(), SUGGESTED_PURCHASE_QTY.getCode(), INVENTORY.getCode());
    }
}
