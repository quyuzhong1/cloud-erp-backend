package com.erp.model.wms.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RuleTypeEnum implements EnumMessage {
    PICKING_STRATEGY("PICKING_STRATEGY", "拣货规则");

    private final String code;
    private final String name;
}
