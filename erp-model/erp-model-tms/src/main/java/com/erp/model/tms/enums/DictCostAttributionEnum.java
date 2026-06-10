package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * 物流标签类型
 */
public enum DictCostAttributionEnum implements EnumMessage {

    FIRST_MILE("firstMile", "头程"),
    SELF_DELIVER("selfDeliver", "自发货"),
    DECLARE("declare", "报关"),
    LAST_MILE("lastMile", "尾程"),
    // 尾程发货仅用于费用管理/导入编辑费用项归属，不作为 logistics_bill_cost.type 使用。
    LAST_MILE_DELIVERY("lastMileDelivery", "尾程发货")
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


    DictCostAttributionEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (DictCostAttributionEnum statusEnum : DictCostAttributionEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}


