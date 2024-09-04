package com.erp.model.mrp.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CfgRuleSettingEnum implements EnumMessage {

    GET_SALES_QTY("GET_SALES_QTY", "获取销量"),

    GET_STOCK_UP("GET_STOCK_UP", "获取备货规则"),
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
