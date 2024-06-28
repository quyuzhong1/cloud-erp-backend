package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 单据类型
 */
public enum CfgRuleOutTypeEnum implements EnumMessage {
    EQUIPMENT_SORTING_PORT("equipmentSortingPort", "设备分拣口"),
    B2C_ALLOWABLE_DEVIATIONS("b2cAllowableDeviations", "B2C称重量方允许偏差"),
    ;
    /**
     * 类型
     */
    @EnumValue
    @JsonValue
    private String code;
    /**
     * 名称
     */
    private String name;


    CfgRuleOutTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }


    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
