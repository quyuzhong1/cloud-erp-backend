package com.erp.model.mrp.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CfgRuleInventoryNodeEnum implements EnumMessage {
    FBA_USABLE("FBA_USABLE","FBA可用"),
    FBA_IN_TRANSIT("FBA_IN_TRANSIT","FBA在途"),
    FBA_ESTIMATED_DELIVERY("FBA_ESTIMATED_DELIVERY","FBA预计发货"),
    FBA_REPLENISHMENT_PLAN("FBA_REPLENISHMENT_PLAN","FBA补货计划"),
    FBA_DELIVERY_PLAN_BY_REPLENISHMENT("FBA_DELIVERY_PLAN_BY_REPLENISHMENT","FBA发货计划_补货计划下推"),
    FBA_DELIVERY_PLAN_BY_MANUAL("FBA_DELIVERY_PLAN_BY_MANUAL","FBA发货计划_手动新增"),
    OVERSEAS_USABLE("OVERSEAS_USABLE", "海外仓可用"),
    OVERSEAS_IN_TRANSIT("OVERSEAS_IN_TRANSIT", "海外仓在途"),
    OVERSEAS_ESTIMATED_DELIVERY("OVERSEAS_ESTIMATED_DELIVERY", "海外仓预计发货"),
    OVERSEAS_REPLENISHMENT_PLAN("OVERSEAS_REPLENISHMENT_PLAN","海外仓补货计划"),
    OVERSEAS_DELIVERY_PLAN_BY_REPLENISHMENT("OVERSEAS_DELIVERY_PLAN_BY_REPLENISHMENT","海外仓发货计划_补货计划下推"),
    OVERSEAS_DELIVERY_PLAN_BY_MANUAL("OVERSEAS_DELIVERY_PLAN_BY_MANUAL","海外仓发货计划_手动新增"),
    LOCAL_USABLE("LOCAL_USABLE", "本地可用"),
    LOCAL_IN_TRANSIT("LOCAL_IN_TRANSIT", "本地在途"),
    LOCAL_ESTIMATED_DELIVERY("LOCAL_ESTIMATED_DELIVERY", "预计采购"),
    LOCAL_REPLENISHMENT_PLAN("LOCAL_REPLENISHMENT_PLAN", "本地补货计划"),
    LOCAL_PURCHASE_PLAN("LOCAL_PURCHASE_PLAN", "本地采购计划"),
    LOCAL_PURCHASE_ORDER("LOCAL_PURCHASE_ORDER", "本地采购单"),
    TOTAL_INVENTORY("TOTAL_INVENTORY", "总库存"),


    FBA_DELIVERY("FBA_DELIVERY","FBA发货单"),
    FBA_SHIPMENT("FBA_SHIPMENT","FBA货件"),
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
