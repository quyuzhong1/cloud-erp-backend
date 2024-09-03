package com.erp.model.mrp.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CfgRulePlatformTypeEnum implements EnumMessage {
    AMAZON("amazon", "AMAZON"),
    OVERSEAS("overseas", "海外"),
    INTERNAL("internal", "国内"),
    B2B("b2b", "B2B"),
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
