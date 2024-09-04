package com.erp.model.mrp.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CfgRuleSettingEnum implements EnumMessage {

    GET_STOCK_UP("GET_STOCK_UP", "获取备货规则"),
    GET_SALES_QTY("GET_SALES_QTY", "获取销量"),
    GET_INVENTORY("GET_INVENTORY", "获取库存"),
    GET_SUGGESTED_AMOUNT("GET_SUGGESTED_AMOUNT", "获取建议量"),
    GET_WAREHOUSE("GET_WAREHOUSE", "获取仓库"),
    GET_REPLENISHMENT_STRATEGY("GET_REPLENISHMENT_STRATEGY", "获取建议策略"),

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
