package com.erp.model.wms.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RuleTypeEnum implements EnumMessage {
    PICKING_STRATEGY("PICKING_STRATEGY", "拣货仓位推荐"),
    CFG_RULE_WAVE("CFG_RULE_WAVE", "波次规则"),
    WAREHOUSE_LOCATION_REPLENISH("WAREHOUSE_LOCATION_REPLENISH", "补货仓位推荐"),
    WAREHOUSE_LOCATION_OUT_STOCK("WAREHOUSE_LOCATION_OUT_STOCK", "出库仓位推荐"),

    ;

    private final String code;
    private final String name;
}
