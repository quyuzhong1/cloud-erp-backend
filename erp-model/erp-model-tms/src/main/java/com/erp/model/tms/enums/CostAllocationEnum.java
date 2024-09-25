package com.erp.model.tms.enums;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 费用分摊
 */
public enum CostAllocationEnum implements EnumMessage {
    WEIGHT_ALLOCATION("weightAllocation","重量分摊"),
    COST_ALLOCATION("costAllocation","成本分摊"),
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


    CostAllocationEnum(String code, String name) {
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
        for (CostAllocationEnum settingEnum : CostAllocationEnum.values()) {
            if (settingEnum.getCode().equals(code)) {
                return settingEnum.getName();
            }
        }
        return "";
    }

    public static CostAllocationEnum getEnum(String code) {
        for (CostAllocationEnum settingEnum : CostAllocationEnum.values()) {
            if (settingEnum.getCode().equals(code)) {
                return settingEnum;
            }
        }
        return null;
    }
}
